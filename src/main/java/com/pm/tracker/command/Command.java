package com.pm.tracker.command;




public interface Command {


    void execute();

    String getCommandType();

    /** Serializes the command payload to a JSON string for the log. */
    String toJson();
}