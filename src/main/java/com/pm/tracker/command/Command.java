package com.pm.tracker.command;


public interface Command {

    void execute();


    void undo();


    String getCommandType();


    String toJson();
}