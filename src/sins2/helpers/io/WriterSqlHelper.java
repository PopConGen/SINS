/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.helpers.io;

import cern.colt.Arrays;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sqlite.SQLiteConfig;
import static sins2.helpers.io.WriterHelper.deleteDir;
import sins2.simulation.Layer;

/**
 *
 * @author tiagomaie <tiagomaie at igc>
 */
public class WriterSqlHelper implements IWriterHelper{

    
    private /*static */Connection _conn=null;
    private static boolean _hasData=false;
    private static boolean _firstrun=false;
    private /*static */PreparedStatement _prep = null;
    private /*static */final ConcurrentHashMap<Integer,Boolean> _hasDData = new ConcurrentHashMap<>();
    
    private /*static */Statement _createTable=null;
    private /*static*/ ResultSet _res=null;
    
    private String _projectName="anonymous";
    private String _rootPath;
    private String _outDir;
    
    private int _simulationId;
    private int _numMarkers;
    private String _geneTable;
    private String _currentMarker;
    
    private Layer[] _layers;
    
    private /*static*/ String _dbconn;
    private final String SQLITEDRIVER = "org.sqlite.JDBC";
    
    private final WriterHelperType _writerType;
    
    private PrintWriter _out;
    
    SQLiteConfig _sqliteconfigs = new SQLiteConfig();
    
    public WriterSqlHelper(String outputDir){
        _outDir = outputDir;
        _sqliteconfigs.setSynchronous(SQLiteConfig.SynchronousMode.NORMAL);
        _sqliteconfigs.setJournalMode(SQLiteConfig.JournalMode.OFF);
        _sqliteconfigs.setPragma(SQLiteConfig.Pragma.FOREIGN_KEYS, "ON");
        _writerType = WriterHelperType.SQL;
    }

    
    @Override
    public WriterHelperType getWriterType(){
        return _writerType;
    }
    
    private void getConnection(int simulationId) throws ClassNotFoundException, SQLException{
        Class.forName(SQLITEDRIVER);
        _conn = DriverManager.getConnection(_dbconn,_sqliteconfigs.toProperties());
        initialize(simulationId);
    }
    
    private void initialize(int simulationId) throws SQLException {

        if(!_hasDData.containsKey(simulationId)){
        
            _hasDData.put(simulationId, true);

            _geneTable = "";
            for (int i = 0; i < _numMarkers; i++) {
                if (i == 0) {//X
                    _geneTable += "X varchar(60),";
                } else if (i == 1) {//Y
                    _geneTable += "Y varchar(60),";
                } else if (i == 2) {//MT
                    _geneTable += "MT varchar(60),";
                } else {//AUTOZOMES
                    _geneTable += "A" + (i - 2) + " varchar(60),";
                }
            }

            //Statement state = _conn.createStatement();
            //ResultSet res = state.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='sinsdata"+/*simulationId+*/"'");
            _createTable = _conn.createStatement();
            _res = _createTable.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='sinsdata'");
            _createTable = _conn.createStatement();
            _res = _createTable.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='sinsstats'");
            
            if (!_res.next()) {
                _createTable = _conn.createStatement();
                
                _createTable.execute("CREATE TABLE IF NOT EXISTS sinsdata "
                        +"("
                        //+ "rowid integer,"
                        + "simulationid integer,"
                        + "generation integer,"
                        + "layer integer,"
                        + "demex integer,"
                        + "demey integer,"
                        + "sex varchar(6),"
                        + "individualid varchar(20),"
                        + "indmother varchar(20),"
                        + "indfather varchar(20),"
                        + _geneTable
                        + "primary key(simulationid, layer, generation, individualid)) WITHOUT ROWID;"
                        //+ "foreign key(simulationid, layer, generation) references sinsdemography(simulationid, layer, generation));"
                );
                _createTable.execute("CREATE TABLE IF NOT EXISTS sinsstats "
                        +"("
                        //+"id integer,"
                        +"simulationid integer,"
                        +"layer integer,"
                        +"generation integer,"
                        +"genmarker varchar(8),"
                        +"heterozygosity varchar(60),"
                        +"ovheterozygosity varchar(60),"
                        +"meanall varchar(60),"
                        +"numall varchar(60),"
                        +"primary key(simulationid, layer, generation, genmarker)) WITHOUT ROWID;"
                        //+"foreign key(simulationid, layer, generation) references sinsdemography(simulationid, layer, generation));"
                );
                _createTable.execute("CREATE TABLE IF NOT EXISTS sinsdemography "
                        +"("
                        //+"id integer,"
                        +"simulationid integer,"
                        +"layer integer,"
                        +"generation integer,"
                        +"matrix varchar(60),"
                        +"primary key(simulationid, layer, generation)) WITHOUT ROWID;"
                        //+"foreign key(simulationid, layer, generation) references sinsdata(simulationid, layer, generation));"
                );
            }        
        }
        _conn.setAutoCommit(false);    
    }

    
    @Override
    public void beginToWriteSimulation(String projectName, int simulationId, Layer... layers) {

        if (projectName.compareTo("") != 0) {
            _projectName = projectName;
        }
        _simulationId = simulationId;
        _rootPath= _outDir + File.separator + _projectName + File.separator;
        
        _dbconn = "jdbc:sqlite:"+_rootPath+"SINS_Data"+_simulationId+".db";
        File dirPopGen = new File(_rootPath);
        
        if (!_firstrun) {
            deleteDir(dirPopGen);
            /*Delete directories that already exist so that we dont
        append simulation outputs by mistake*/
            dirPopGen.mkdirs();
        }

        _numMarkers = 0;
        for (int i = 0; i < layers.length; i++) {
            Layer layer = layers[i];
            if(_numMarkers <layer.getGenotypeInformation().getNbAutosomes())
                _numMarkers=layer.getGenotypeInformation().getNbAutosomes();
        }
        _numMarkers += 3;
        
        try {
            _conn = null;
            Class.forName(SQLITEDRIVER);
            _conn = DriverManager.getConnection(_dbconn,_sqliteconfigs.toProperties());
            initialize(_simulationId);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(WriterSqlHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(WriterSqlHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
     
    }


    @Override
    public void closeCurrentRecord() {
        try {
            _prep.executeBatch();
            _conn.commit();

        } catch (SQLException ex) {
            Logger.getLogger(WriterSqlHelper.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                _prep.close();
            } catch (SQLException ex) {
                Logger.getLogger(WriterSqlHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void finishedWriteOutput() {
        try {
            _conn.close();
            _firstrun=true;
        } catch (SQLException ex) {
            Logger.getLogger(WriterSqlHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void openSumStatsRecord(String geneName, int generation) {
        try {
            if (_conn == null) {
                getConnection(_simulationId);
            }
            _prep = _conn.prepareStatement("INSERT INTO sinsstats values(?,?,?,?,?,?,?,?);");
        } catch (SQLException ex) {
            Logger.getLogger(WriterSqlHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(WriterSqlHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void closeSumStatsCurrentRecord() {
        try {
            _prep.executeBatch();
            _conn.commit();

        } catch (SQLException ex) {
            Logger.getLogger(WriterSqlHelper.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                _prep.close();
            } catch (SQLException ex) {
                Logger.getLogger(WriterSqlHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }    
    }
    
    
    @Override
    public void openRecord(String geneName, int generation) {
        try {
            if (_conn == null) {
                getConnection(_simulationId);
            }
            String markerStub = "";
            for (int i = 0; i < _numMarkers; i++) {
                markerStub += ",?";
            }
            _prep = _conn.prepareStatement("INSERT INTO sinsdata values(?,?,?,?,?,?,?,?,?"+markerStub+");");
        } catch (SQLException ex) {
            Logger.getLogger(WriterSqlHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(WriterSqlHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    

    @Override
    public void printLine(int geneId, String geneReprs) {
        try {
            //System.out.println(geneReprs);
            //addData(geneId, geneReprs);
            addSingleIndData(geneReprs);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(WriterSqlHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(WriterSqlHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addData(int geneId,String simdata) throws ClassNotFoundException, SQLException{

        String[] tokens = simdata.trim().split(" ");
        
        if (!(geneId == 1 || geneId == 0)) {
            if (geneId == 2) {
                _prep.setInt(2, _simulationId);
                
                for (int i = 0; i < tokens.length; i++) {
                    if(i == tokens.length - 1){
                        _prep.setString(i + 5, tokens[i]);
                    }else{
                        _prep.setString(i + 3, tokens[i]);
                    }
                }
            } else {
                _prep.setString(1,tokens[tokens.length - 1]);
                _prep.setString(2,tokens[3]);
                _prep.setInt(3,Integer.parseInt(tokens[0]));
                _prep.setInt(4,_simulationId);
            }
            _prep.addBatch();
        }
    }
    
    private void addSingleIndData(String simdata) throws ClassNotFoundException, SQLException{
        String[] tokens = simdata.trim().split(" ");
        
        _prep.setInt(1, _simulationId);
        for (int i = 0; i < tokens.length; i++) {
            _prep.setString(i + 2, tokens[i]);
        }
        _prep.addBatch();
    }
    
    
    @Override
    public void printSizeMatrix(int[][] popSize, int currentGeneration, int simulationId, String _layerName, int layerId) {
        
        try {
            if (_conn == null) {
                getConnection(_simulationId);
            }
            _prep = _conn.prepareStatement("INSERT INTO sinsdemography values(?,?,?,?);");
            _prep.setInt(1, _simulationId);
            _prep.setInt(2, layerId);
            _prep.setInt(3, currentGeneration);
            
            String demoMap = "";
            for (int i = 0; i < popSize.length; i++) {
                int[] is = popSize[i];
                for (int j = 0; j < is.length; j++) {
                    int k = is[j];
                    demoMap += k + " ";
                }
                demoMap += "\n";
                //demoMap += "| ";
            }
            _prep.setString(4, demoMap);
            _prep.addBatch();
            
            _prep.executeBatch();
            _conn.commit();
        } catch (SQLException ex) {
            Logger.getLogger(WriterSqlHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(WriterSqlHelper.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            try {
                _prep.close();
            } catch (SQLException ex) {
                Logger.getLogger(WriterSqlHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }

    @Override
    public void printSummaryStatsHeader(String[] statNames) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void printSummaryStatsLine(String stats) {
        try {
            //all stats already come bundled up per generation for all markers
            String[] lines = stats.split("\r\n");
            for (String line : lines) {
                _prep.setInt(1, _simulationId);
                String[] tokens = line.split("\t");
                for (int i = 0; i < tokens.length; i++) {
                    _prep.setString(i + 2, tokens[i]);
                }
                _prep.addBatch();
            }
        } catch (SQLException ex) {
            Logger.getLogger(WriterSqlHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setOutPutDir(String outputDir) {
        _outDir=outputDir;
    }

    @Override
    public void printSimulationOptions(LinkedHashMap<String, HashMap> projOptions){
        String optFilePath = _outDir + File.separator + _projectName + File.separator + _projectName + "_options.txt";
                
        try {
            _out=new PrintWriter(new BufferedWriter(new FileWriter(optFilePath,false)));
            for (String option : projOptions.keySet()) {
                _out.write(option + " " + projOptions.get(option));
                _out.write("\n\n");
            }
            _out.close();
        } 
        catch (IOException ex) {
            Logger.getLogger(WriterHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setProjectName(String _projectName) {
        this._projectName = _projectName;
    }
    
}
