package com.oracle.cep.cartridge.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import oracle.cep.extensibility.datasource.IArchiverQueryResult;

class ArchiverResultImpl
    implements IArchiverQueryResult
{
    private Connection m_connection;
    private ResultSet[] m_resultSets;

    public ArchiverResultImpl(Connection connection, ResultSet[] resultSets)
    {
        m_connection = connection;
        m_resultSets = resultSets;
    }

    public int getResultCount()
    {
        return m_resultSets.length;
    }

    public ResultSet getResult(int index)
    {   //OK to not check if the index is out of range, because it is 
        //a coding error if it is out of index and hence OK to throw 
        //the default exception that gets thrown from Java.
        return m_resultSets[index];
    }
}
