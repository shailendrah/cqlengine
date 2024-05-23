package com.oracle.cep.cartridge.jdbc;

import java.util.Map;

import com.bea.wlevs.ede.api.StageFactory;
import com.bea.wlevs.ede.spi.TableStage;

/**
 * Stage factory for Tables. 
 */
public class TableStageFactory
    implements StageFactory<TableStage>
{
    public TableStage create()
    {
         return new TableExternalSourceStage();
    }
}
