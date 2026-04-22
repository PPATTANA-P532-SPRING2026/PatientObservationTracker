// ── Config ────────────────────────────────────────────────────────────
const API_BASE = 'https://patientobservationtracker-2.onrender.com';

function api(path, options) {
    return fetch(`${API_BASE}${path}`, options);
}

// ── Shared utilities ──────────────────────────────────────────────────
function showMsg(elementId, message, type) {
    const el = document.getElementById(elementId);
    if (!el) return;
    el.innerHTML = `<div class="alert alert-${type}">${message}</div>`;
    setTimeout(() => { el.innerHTML = ''; }, 4000);
}

function formatDt(dt) {
    if (!dt) return '—';
    try {
        const d = new Date(dt);
        return d.toLocaleDateString('en-US', {
            month: 'short', day: 'numeric', year: 'numeric'
        }) + ' ' + d.toLocaleTimeString('en-US', {
            hour: '2-digit', minute: '2-digit'
        });
    } catch { return dt; }
}

function formatJson(raw) {
    try { return JSON.stringify(JSON.parse(raw), null, 2); }
    catch { return raw; }
}

// ── User session ──────────────────────────────────────────────────────
function getCurrentUser() {
    return localStorage.getItem('currentUser') || 'staff';
}

function setCurrentUser() {
    const sel = document.getElementById('userSelect');
    if (!sel || !sel.value) return;
    localStorage.setItem('currentUser', sel.value);
}

function loadUserDropdown() {
    const sel = document.getElementById('userSelect');
    if (!sel) return;
    api('/api/users')
        .then(r => r.json())
        .then(users => {
            const current = getCurrentUser();
            sel.innerHTML = '<option value="">Select user...</option>';
            users.forEach(u => {
                sel.innerHTML += `<option value="${u.username}"
                    ${u.username === current ? 'selected' : ''}>
                    ${u.username} (${u.role})</option>`;
            });
        })
        .catch(() => {});
}

// ── Router ────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    const page = window.location.pathname.split('/').pop() || 'index.html';
    if (page === 'index.html' || page === '') initIndex();
    else if (page === 'patient.html')          initPatient();
    else if (page === 'catalogue.html')        initCatalogue();
    else if (page === 'logs.html')             initLogs();
});

// ═════════════════════════════════════════════════════════════════════
// INDEX PAGE
// ═════════════════════════════════════════════════════════════════════
function initIndex() {
    loadUserDropdown();
    loadPatients();
}

function loadPatients() {
    api('/api/patients')
        .then(r => r.json())
        .then(patients => {
            const el = document.getElementById('patientList');
            if (!el) return;
            if (!patients.length) {
                el.innerHTML = '<p class="muted">No patients yet.</p>';
                return;
            }
            el.innerHTML = patients.map(p => `
                <div class="patient-item"
                     onclick="location.href='patient.html?id=${p.id}'">
                    <div>
                        <div class="patient-name">${p.fullName}</div>
                        <div class="patient-dob">DOB: ${p.dateOfBirth}</div>
                        ${p.note ? `<div class="small muted">${p.note}</div>` : ''}
                    </div>
                    <span class="btn btn-ghost btn-sm">View</span>
                </div>
            `).join('');
        })
        .catch(() => {
            const el = document.getElementById('patientList');
            if (el) el.innerHTML = '<p class="muted">Could not load patients.</p>';
        });
}

function addPatient() {
    const fullName = document.getElementById('fullName').value.trim();
    const dob      = document.getElementById('dob').value;
    const note     = document.getElementById('note').value.trim();
    if (!fullName || !dob) {
        showMsg('addMsg', 'Full name and date of birth are required.', 'error');
        return;
    }
    api('/api/patients', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Current-User': getCurrentUser() },
        body: JSON.stringify({ fullName, dateOfBirth: dob, note })
    })
    .then(r => r.json())
    .then(p => {
        if (p.error) { showMsg('addMsg', p.error, 'error'); return; }
        showMsg('addMsg', `Patient "${p.fullName}" created.`, 'success');
        document.getElementById('fullName').value = '';
        document.getElementById('dob').value      = '';
        document.getElementById('note').value     = '';
        loadPatients();
    })
    .catch(() => showMsg('addMsg', 'Error creating patient.', 'error'));
}

// ═════════════════════════════════════════════════════════════════════
// PATIENT PAGE
// ═════════════════════════════════════════════════════════════════════
function initPatient() {
    loadUserDropdown();
    const params    = new URLSearchParams(location.search);
    const patientId = params.get('id');
    if (!patientId) { location.href = '/'; return; }
    window.currentPatientId = patientId;
    Promise.all([
        api(`/api/patients/${patientId}`).then(r => r.json()),
        api('/api/phenomenon-types').then(r => r.json()),
        api('/api/protocols').then(r => r.json())
    ]).then(([patient, types, protocols]) => {
        renderPatientHeader(patient);
        populateMeasTypes(types.filter(t => t.kind === 'QUANTITATIVE'));
        populatePhenomena(types.filter(t => t.kind === 'QUALITATIVE'));
        populateProtocols(protocols);
    });
    loadObservations();
}

function renderPatientHeader(p) {
    document.getElementById('patientHeader').innerHTML = `
        <div style="display:flex;align-items:baseline;gap:16px">
            <h1 style="margin:0">${p.fullName}</h1>
            <span class="mono muted">DOB: ${p.dateOfBirth}</span>
        </div>
        ${p.note ? `<p class="muted small" style="margin-top:6px">${p.note}</p>` : ''}
    `;
    document.title = `${p.fullName} — POTracker`;
}

function populateMeasTypes(types) {
    const sel = document.getElementById('measType');
    if (!sel) return;
    types.forEach(t => {
        sel.innerHTML += `<option value="${t.id}"
            data-units='${JSON.stringify(t.allowedUnits || [])}'>${t.name}</option>`;
    });
    sel.addEventListener('change', () => {
        const opt   = sel.options[sel.selectedIndex];
        const units = JSON.parse(opt.getAttribute('data-units') || '[]');
        const uSel  = document.getElementById('measUnit');
        uSel.innerHTML = units.map(u => `<option>${u}</option>`).join('')
                         || '<option>No units</option>';
    });
}

function populatePhenomena(qualTypes) {
    const sel = document.getElementById('catPhenomenon');
    if (!sel) return;
    qualTypes.forEach(t => {
        (t.phenomena || []).forEach(ph => {
            sel.innerHTML +=
                `<option value="${ph.id}">${t.name} — ${ph.name}</option>`;
        });
    });
}

function populateProtocols(protocols) {
    ['measProtocol', 'catProtocol'].forEach(id => {
        const sel = document.getElementById(id);
        if (!sel) return;
        protocols.forEach(p => {
            sel.innerHTML +=
                `<option value="${p.id}">${p.name} (${p.accuracyRating})</option>`;
        });
    });
}

function loadObservations() {
    const patientId = window.currentPatientId;
    api(`/api/patients/${patientId}/observations`)
        .then(r => r.json())
        .then(renderObservations)
        .catch(() => {
            const el = document.getElementById('obsList');
            if (el) el.innerHTML = '<p class="muted">Could not load observations.</p>';
        });
}

function renderObservations(obs) {
    const el = document.getElementById('obsList');
    if (!el) return;
    if (!obs.length) { el.innerHTML = '<p class="muted">No observations yet.</p>'; return; }
    el.innerHTML = `
        <table>
            <thead><tr>
                <th>Type</th><th>Value</th><th>Recorded</th><th>Status</th><th></th>
            </tr></thead>
            <tbody>
                ${obs.map(o => {
                    const isM   = o.amount !== undefined;
                    const value = isM ? `${o.amount} ${o.unit}` : `${o.phenomenon?.name} — ${o.presence}`;
                    const typeName = isM ? (o.phenomenonType?.name || '—') : (o.phenomenon?.phenomenonType?.name || '—');
                    const anomalyBadge = o.anomalyFlagged ? `<span class="badge badge-rejected">⚠ ANOMALY</span>` : '';
                    const inferredStyle = o.source === 'INFERRED' ? 'font-style:italic;color:var(--muted)' : '';
                    const badge = o.status === 'ACTIVE'
                        ? `<span class="badge badge-active">ACTIVE</span>`
                        : `<span class="badge badge-rejected">REJECTED</span>`;
                    const inferredBadge = o.source === 'INFERRED'
                        ? `<span class="badge" style="background:#f0f0f0;color:var(--muted)">INFERRED</span>` : '';
                    const rejectBtn = o.status === 'ACTIVE'
                        ? `<button class="btn btn-danger btn-sm" onclick="rejectObs('${o.id}')">Reject</button>` : '';
                    return `
                        <tr style="${inferredStyle}">
                            <td><span class="small muted">${typeName}</span></td>
                            <td class="mono">${value} ${anomalyBadge}</td>
                            <td class="small muted">${formatDt(o.recordingTime)}</td>
                            <td>${badge} ${inferredBadge}</td>
                            <td>${rejectBtn}</td>
                        </tr>
                        ${o.anomalyDetail ? `<tr><td colspan="5" class="small muted" style="padding-top:0;color:var(--danger)">⚠ ${o.anomalyDetail}</td></tr>` : ''}
                        ${o.rejectionReason ? `<tr><td colspan="5" class="small muted" style="padding-top:0">↳ ${o.rejectionReason}</td></tr>` : ''}
                    `;
                }).join('')}
            </tbody>
        </table>
    `;
}

function recordMeasurement() {
    const patientId        = window.currentPatientId;
    const phenomenonTypeId = document.getElementById('measType').value;
    const amount           = document.getElementById('measAmount').value;
    const unit             = document.getElementById('measUnit').value;
    const protocolId       = document.getElementById('measProtocol').value || null;
    const rawApply         = document.getElementById('measApply').value;
    const applicabilityTime = rawApply ? rawApply + ':00' : null;
    if (!phenomenonTypeId || !amount || !unit) {
        showMsg('measMsg', 'Type, amount and unit are required.', 'error'); return;
    }
    api('/api/observations/measurement', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Current-User': getCurrentUser() },
        body: JSON.stringify({ patientId, phenomenonTypeId, amount, unit, protocolId, applicabilityTime })
    })
    .then(r => r.json())
    .then(res => {
        if (res.error) { showMsg('measMsg', res.error, 'error'); return; }
        showMsg('measMsg', 'Measurement recorded.', 'success');
        document.getElementById('measAmount').value = '';
        loadObservations();
    })
    .catch(() => showMsg('measMsg', 'Error recording measurement.', 'error'));
}

function recordCategory() {
    const patientId    = window.currentPatientId;
    const phenomenonId = document.getElementById('catPhenomenon').value;
    const presence     = document.getElementById('catPresence').value;
    const protocolId   = document.getElementById('catProtocol').value || null;
    const rawApply     = document.getElementById('catApply').value;
    const applicabilityTime = rawApply ? rawApply + ':00' : null;
    if (!phenomenonId) { showMsg('catMsg', 'Phenomenon is required.', 'error'); return; }
    api('/api/observations/category', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Current-User': getCurrentUser() },
        body: JSON.stringify({ patientId, phenomenonId, presence, protocolId, applicabilityTime })
    })
    .then(r => r.json())
    .then(res => {
        if (res.error) { showMsg('catMsg', res.error, 'error'); return; }
        showMsg('catMsg', 'Observation recorded.', 'success');
        loadObservations();
    })
    .catch(() => showMsg('catMsg', 'Error recording observation.', 'error'));
}

function rejectObs(id) {
    const reason = prompt('Rejection reason:');
    if (reason === null) return;
    api(`/api/observations/${id}/reject`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Current-User': getCurrentUser() },
        body: JSON.stringify({ reason: reason || 'No reason provided' })
    })
    .then(() => loadObservations())
    .catch(() => alert('Error rejecting observation.'));
}

function evaluateRules() {
    const patientId = window.currentPatientId;
    api(`/api/patients/${patientId}/evaluate`, { method: 'POST' })
        .then(r => r.json())
        .then(res => {
            const panel = document.getElementById('inferencePanel');
            const list  = document.getElementById('inferenceList');
            panel.style.display = 'block';
            if (!res.inferences || !res.inferences.length) {
                list.innerHTML = '<p class="muted">No rules fired for current observations.</p>';
                return;
            }
            list.innerHTML = res.inferences.map(i => `
                <div class="inference-item">
                    <strong>${i.inferredConcept}</strong> inferred
                    <span class="badge badge-quant" style="margin-left:8px">${i.strategyUsed}</span>
                    <span class="small muted" style="margin-left:8px">${i.evidenceCount} evidence observation(s)</span>
                </div>
            `).join('');
        })
        .catch(() => alert('Error evaluating rules.'));
}

// ═════════════════════════════════════════════════════════════════════
// CATALOGUE PAGE
// ═════════════════════════════════════════════════════════════════════
let allTypes = [];

function initCatalogue() {
    loadUserDropdown();
    loadCatalogue();
}

function loadCatalogue() {
    api('/api/phenomenon-types').then(r => r.json()).then(types => {
        allTypes = types;
        renderTypes(types);
        populateRuleSelects(types);
    });
    api('/api/protocols').then(r => r.json()).then(renderProtocols);
    api('/api/associative-functions').then(r => r.json()).then(renderRules);
}

function toggleKindFields() {
    const kind = document.getElementById('ptKind').value;
    document.getElementById('quantFields').style.display = kind === 'QUANTITATIVE' ? '' : 'none';
    document.getElementById('qualFields').style.display  = kind === 'QUALITATIVE'  ? '' : 'none';
}

function toggleThreshold() {
    const strategy = document.getElementById('ruleStrategy').value;
    document.getElementById('thresholdField').style.display = strategy === 'WEIGHTED' ? '' : 'none';
}

function renderTypes(types) {
    const el = document.getElementById('ptList');
    if (!el) return;
    if (!types.length) { el.innerHTML = '<p class="muted">None yet.</p>'; return; }
    el.innerHTML = `<table><thead><tr><th>Name</th><th>Kind</th><th>Details</th></tr></thead><tbody>
        ${types.map(t => {
            const badge = t.kind === 'QUANTITATIVE'
                ? '<span class="badge badge-quant">QUANT</span>'
                : '<span class="badge badge-qual">QUAL</span>';
            const detail = t.kind === 'QUANTITATIVE'
                ? (t.allowedUnits || []).join(', ')
                : (t.phenomena || []).map(p => p.name).join(', ');
            return `<tr><td>${t.name}</td><td>${badge}</td><td class="small muted">${detail}</td></tr>`;
        }).join('')}
    </tbody></table>`;
}

function renderProtocols(protocols) {
    const el = document.getElementById('protoList');
    if (!el) return;
    if (!protocols.length) { el.innerHTML = '<p class="muted">None yet.</p>'; return; }
    el.innerHTML = `<table><thead><tr><th>Name</th><th>Accuracy</th><th>Description</th></tr></thead><tbody>
        ${protocols.map(p => `<tr>
            <td>${p.name}</td>
            <td><span class="badge badge-active">${p.accuracyRating}</span></td>
            <td class="small muted">${p.description || ''}</td>
        </tr>`).join('')}
    </tbody></table>`;
}

function renderRules(rules) {
    const el = document.getElementById('ruleList');
    if (!el) return;
    if (!rules.length) { el.innerHTML = '<p class="muted">None yet.</p>'; return; }
    el.innerHTML = `<table><thead><tr>
        <th>Rule</th><th>Strategy</th><th>Arguments</th><th>Infers</th>
    </tr></thead><tbody>
        ${rules.map(r => `<tr>
            <td>${r.name}</td>
            <td><span class="badge badge-quant">${r.strategyType || 'CONJUNCTIVE'}</span></td>
            <td class="small">${(r.argumentConcepts || []).map(c => c.name).join(' + ')}</td>
            <td><strong>${r.productConcept?.name || '—'}</strong></td>
        </tr>`).join('')}
    </tbody></table>`;
}

function populateRuleSelects(types) {
    const args = document.getElementById('ruleArgs');
    const prod = document.getElementById('ruleProduct');
    if (!args || !prod) return;
    args.innerHTML = '';
    prod.innerHTML = '';
    types.forEach(t => {
        args.innerHTML += `<option value="${t.id}">${t.name}</option>`;
        prod.innerHTML += `<option value="${t.id}">${t.name}</option>`;
    });
}

function addPhenomenonType() {
    const name = document.getElementById('ptName').value.trim();
    const kind = document.getElementById('ptKind').value;
    if (!name) { showMsg('ptMsg', 'Name is required.', 'error'); return; }
    const body = { name, kind };
    if (kind === 'QUANTITATIVE') {
        body.allowedUnits = document.getElementById('ptUnits').value
            .split(',').map(s => s.trim()).filter(Boolean);
    } else {
        body.phenomena = document.getElementById('ptPhenomena').value
            .split(',').map(s => s.trim()).filter(Boolean);
    }
    api('/api/phenomenon-types', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    })
    .then(r => r.json())
    .then(res => {
        if (res.error) { showMsg('ptMsg', res.error, 'error'); return; }
        showMsg('ptMsg', 'Phenomenon type created.', 'success');
        document.getElementById('ptName').value      = '';
        document.getElementById('ptUnits').value     = '';
        document.getElementById('ptPhenomena').value = '';
        loadCatalogue();
    })
    .catch(() => showMsg('ptMsg', 'Error.', 'error'));
}

function addProtocol() {
    const name           = document.getElementById('protoName').value.trim();
    const description    = document.getElementById('protoDesc').value.trim();
    const accuracyRating = document.getElementById('protoRating').value;
    if (!name) { showMsg('protoMsg', 'Name is required.', 'error'); return; }
    api('/api/protocols', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, description, accuracyRating })
    })
    .then(r => r.json())
    .then(res => {
        if (res.error) { showMsg('protoMsg', res.error, 'error'); return; }
        showMsg('protoMsg', 'Protocol created.', 'success');
        document.getElementById('protoName').value = '';
        document.getElementById('protoDesc').value = '';
        loadCatalogue();
    })
    .catch(() => showMsg('protoMsg', 'Error.', 'error'));
}

function addRule() {
    const name             = document.getElementById('ruleName').value.trim();
    const args             = document.getElementById('ruleArgs');
    const productConceptId = document.getElementById('ruleProduct').value;
    const strategyType     = document.getElementById('ruleStrategy').value;
    const threshold        = parseFloat(document.getElementById('ruleThreshold').value || '0.5');
    const argumentConceptIds = Array.from(args.selectedOptions).map(o => o.value);
    if (!name || !argumentConceptIds.length || !productConceptId) {
        showMsg('ruleMsg', 'Name, at least one argument, and product are required.', 'error'); return;
    }
    const argumentWeights = {};
    argumentConceptIds.forEach(id => { argumentWeights[id] = 1.0; });
    api('/api/associative-functions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, argumentConceptIds, productConceptId, strategyType, threshold, argumentWeights })
    })
    .then(r => r.json())
    .then(res => {
        if (res.error) { showMsg('ruleMsg', res.error, 'error'); return; }
        showMsg('ruleMsg', 'Rule created.', 'success');
        document.getElementById('ruleName').value = '';
        loadCatalogue();
    })
    .catch(() => showMsg('ruleMsg', 'Error.', 'error'));
}

// ═════════════════════════════════════════════════════════════════════
// LOGS PAGE
// ═════════════════════════════════════════════════════════════════════
function initLogs() {
    loadUserDropdown();
    loadUsers();
    loadLogs();
}

function loadLogs() {
    api('/api/command-log').then(r => r.json()).then(renderCommandLog)
        .catch(() => {
            const el = document.getElementById('commandLog');
            if (el) el.innerHTML = '<p class="muted">Could not load command log.</p>';
        });
    api('/api/audit-log').then(r => r.json()).then(renderAuditLog)
        .catch(() => {
            const el = document.getElementById('auditLog');
            if (el) el.innerHTML = '<p class="muted">Could not load audit log.</p>';
        });
}

function renderCommandLog(entries) {
    const el = document.getElementById('commandLog');
    if (!el) return;
    if (!entries.length) { el.innerHTML = '<p class="muted">No commands yet.</p>'; return; }
    el.innerHTML = entries.map(e => `
        <div class="card" style="margin-bottom:12px;padding:14px 16px">
            <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:8px">
                <span class="badge badge-quant">${e.commandType}</span>
                <div style="display:flex;gap:8px;align-items:center">
                    ${!e.undone && e.commandType !== 'CREATE_PATIENT'
                        ? `<button class="btn btn-danger btn-sm" onclick="undoCommand('${e.id}')">Undo</button>` : ''}
                    ${e.undone ? `<span class="badge badge-rejected">UNDONE</span>` : ''}
                    <span class="mono small muted">${formatDt(e.executedAt)}</span>
                </div>
            </div>
            <div class="small muted">User: <strong>${e.user}</strong></div>
            <details style="margin-top:8px">
                <summary class="small" style="cursor:pointer;color:var(--accent)">View payload</summary>
                <pre style="margin-top:8px;font-size:11px;background:var(--bg);padding:10px;border-radius:4px;overflow:auto;white-space:pre-wrap">${formatJson(e.payload)}</pre>
            </details>
        </div>
    `).join('');
}

function undoCommand(id) {
    if (!confirm('Are you sure you want to undo this command?')) return;
    api(`/api/command-log/${id}/undo`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ user: getCurrentUser() })
    })
    .then(r => r.json())
    .then(res => {
        if (res.error) { alert(res.error); return; }
        loadLogs();
    })
    .catch(() => alert('Error undoing command.'));
}

function renderAuditLog(entries) {
    const el = document.getElementById('auditLog');
    if (!el) return;
    if (!entries.length) { el.innerHTML = '<p class="muted">No audit entries yet.</p>'; return; }
    el.innerHTML = entries.map(e => {
        const badgeClass = e.event.includes('REJECTED') ? 'badge-rejected'
                         : e.event.includes('INFERRED') ? 'badge-qual'
                         : 'badge-active';
        return `
            <div class="card" style="margin-bottom:12px;padding:14px 16px">
                <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:8px">
                    <span class="badge ${badgeClass}">${e.event}</span>
                    <span class="mono small muted">${formatDt(e.timestamp)}</span>
                </div>
                ${e.detail ? `<div class="small muted">${e.detail}</div>` : ''}
                ${e.observationId ? `<div class="mono small muted" style="margin-top:4px">Obs: ${e.observationId}</div>` : ''}
            </div>
        `;
    }).join('');
}

function loadUsers() {
    api('/api/users').then(r => r.json()).then(users => {
        const el = document.getElementById('userList');
        if (!el) return;
        if (!users.length) { el.innerHTML = '<p class="muted small">No users yet.</p>'; return; }
        el.innerHTML = `<table><thead><tr><th>Username</th><th>Role</th></tr></thead><tbody>
            ${users.map(u => `<tr>
                <td class="mono">${u.username}</td>
                <td><span class="badge ${u.role === 'ADMIN' ? 'badge-rejected' : 'badge-active'}">${u.role}</span></td>
            </tr>`).join('')}
        </tbody></table>`;
    });
}

function createUser() {
    const username = document.getElementById('newUsername').value.trim();
    const role     = document.getElementById('newRole').value;
    if (!username) { showMsg('userMsg', 'Username is required.', 'error'); return; }
    api('/api/users', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, role })
    })
    .then(r => r.json())
    .then(res => {
        if (res.error) { showMsg('userMsg', res.error, 'error'); return; }
        showMsg('userMsg', `User "${res.username}" created.`, 'success');
        document.getElementById('newUsername').value = '';
        loadUsers();
        loadUserDropdown();
    })
    .catch(() => showMsg('userMsg', 'Error creating user.', 'error'));
}