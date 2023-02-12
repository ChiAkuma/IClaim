package de.chiakuma.iclaim.utils;

import de.chiakuma.iclaim.IClaim;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseTools
{
    Connection connection;
    String driver;
    
    public DatabaseTools(String driver, String address, int port, String user, String password, String database)
    {
        this.driver = driver;
        this.connect(driver, address, port, user, password, database);
        this.createTables();
    }
    
    public void connect(String driver, String address, int port, String user, String password, String database)
    {
        String url = switch (driver)
                {
                    case "mariadb"  -> "jdbc:mariadb://%s:%s/%s".formatted(address, port, database);
                    case "mysql"    -> "jdbc:mysql://%s:%s/%s".formatted(address, port, database);
                    default         -> "";
                };
    
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Class.forName("org.mariadb.jdbc.Driver");
            this.connection = DriverManager.getConnection(url, user, password);
            IClaim.PLUGIN.getLogger().info("Database connection successfully");
        }
        catch (SQLException e)
        {
            IClaim.PLUGIN.getLogger().warning("Database connection failed");
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public ArrayList<ResultSet> executeQuerys(String... sql)
    {
        ArrayList<ResultSet> resultSets = new ArrayList<>();
        for (String cache : sql)
        {
            resultSets.add(executeQuery(cache));
        }
    
        return resultSets;
    }
    
    public ResultSet executeQuery(String sql)
    {
        try
        {
            Statement statement = connection.createStatement();
            return statement.executeQuery(sql);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    public boolean createTables()
    {
        //Creating all the tables for this plugin
        String[] query = new String[5];
        query[0] = "CREATE TABLE IF NOT EXISTS IClaim_players(" +
                "playerID INT NOT NULL AUTO_INCREMENT, " +
                "playerName TEXT, " +
                "playerUUID UUID, " +
                "PRIMARY KEY (playerID))";
        query[1] = "CREATE TABLE IF NOT EXISTS IClaim_claims(" +
                "claimID INT NOT NULL AUTO_INCREMENT, " +
                "playerID INT REFERENCES IClaim_players(playerID), " +
                "claimName TEXT, " +
                "claimSize INT, " +
                "pos1 TEXT, " +
                "pos2 TEXT, " +
                "topToBottom BOOL, " +
                "PRIMARY KEY (claimID))";
        query[2] = "CREATE TABLE IF NOT EXISTS IClaim_claim_builders(" +
                "claimID INT REFERENCES IClaim_claims(claimID), " +
                "builder INT REFERENCES IClaim_players(playerID))";
        query[3] = "CREATE TABLE IF NOT EXISTS IClaim_claim_banned(" +
                "claimID INT REFERENCES IClaim_claims(claimID), " +
                "bannedPlayer INT REFERENCES IClaim_players(playerID))";
        query[4] = "CREATE TABLE IF NOT EXISTS IClaim_auctionHouse(" +
                "auctionID INT NOT NULL AUTO_INCREMENT, " +
                "claimID INT REFERENCES IClaim_claims(claimID), " +
                "startingPrice INT, " +
                "AllowBID BOOL, " +
                "BIDNumber INT, " +
                "highestBID INT, " +
                "highestBIDPlayer INT REFERENCES IClaim_players(PlayerID), " +
                "spectatePos TEXT, " +
                "soldToPlayer INT REFERENCES IClaim_players(PlayerID), " +
                "forSale BOOL, " +
                "created DATE, " +
                "PRIMARY KEY (auctionID))";
        query[5] = "CREATE TABLE IF NOT EXISTS IClaim_auctionHouse_BIDs(" +
                "playerID INT REFERENCES IClaim_players(playerID), " +
                "auctionID INT REFERENCES IClaim_auctionHouse(auctionID), " +
                "BIDAmount INT, " +
                "status TEXT, " +
                "BIDTimestamp DATE)";
        this.executeQuerys(query);
        IClaim.PLUGIN.getLogger().warning("Tables created successful");
        
        String f1 = "Nobody", f2 = "00000000-0000-0000-0000-000000000000";
        boolean hasContent = false;
        try
        {
            ResultSet result = this.executeQuery("SELECT * FROM IClaim_players WHERE (playerUUID=\"" + f2 + "\")");
            hasContent = result.last();
            result.close();
            IClaim.PLUGIN.getLogger().warning("Tables warning: " + hasContent);
        }
        catch (SQLException | NullPointerException ignored) {}
        if (!hasContent)
        {
            this.executeQuery("INSERT INTO IClaim_players (playerName, playerUUID) VALUES (\"%s\", \"%s\")".formatted(f1, f2));
            IClaim.PLUGIN.getLogger().warning("Tables created REALLY successful");
        }
        
        return true;
    }
}
