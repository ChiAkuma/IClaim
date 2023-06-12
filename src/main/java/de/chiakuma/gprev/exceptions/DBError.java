package de.chiakuma.gprev.exceptions;

public class DBError extends Exception{
    public DBError(String errorMsg)
    {
        super(errorMsg);
        //TODO: log error to file
    }
}
