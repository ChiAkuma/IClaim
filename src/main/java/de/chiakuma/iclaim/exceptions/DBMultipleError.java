package de.chiakuma.iclaim.exceptions;

public class DBMultipleError extends Exception
{
    public DBMultipleError(String errorMsg)
    {
        super(errorMsg);
        //TODO: log error to file
    }
}
