package de.chiakuma.iclaim.utils;

import de.chiakuma.iclaim.Plugin_IClaim;
import de.chiakuma.iclaim.exceptions.DBError;
import de.chiakuma.iclaim.exceptions.DBMultipleError;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class DatabaseTools
{
    Connection connection;
    String driver;
    String prefix = "IClaim";
    
    public DatabaseTools(String driver, String address, int port, String user, String password, String database)
    {
        this.driver = driver;
        this.connect(driver, address, port, user, password, database);
        this.createTables();
    }

    /**
     * Connect to a database
     * @param driver The driver in charge
     * @param address the address for the server
     * @param port the port for the server
     * @param user the username for the server
     * @param password the password for the server
     * @param database the database for the server
     */
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
            Plugin_IClaim.PLUGIN.getLogger().info("Database connection successfully");
        }
        catch (SQLException e)
        {
            Plugin_IClaim.PLUGIN.getLogger().warning("Database connection failed");
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes multiple sql codes at once
     * @param sql String array. No defined length
     * @return a list of all the result sets in an ArrayList
     */
    public ArrayList<ResultSet> executeQuerys(ArrayList<String> sql)
    {
        ArrayList<ResultSet> resultSets = new ArrayList<>();
        for (String cache : sql)
        {
            resultSets.add(executeQuery(cache));
        }
    
        return resultSets;
    }

    /**
     * Executes one single sql code
     * @param sql
     * @return
     */
    public ResultSet executeQuery(String sql)
    {
        System.out.println(sql);
        try
        {
            sql = sql.replaceAll("\\?PRE\\?", this.prefix);
            System.out.println(sql);
            Statement statement = connection.createStatement();
            return statement.executeQuery(sql);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates all necessary tables for this plugin
     * @return True if successfully created
     */
    public boolean createTables()
    {
        //Creating all the tables for this plugin
        ArrayList<String> query = new ArrayList<>();

        //================================================================================
        // Table for players
        //================================================================================
        query.add("CREATE TABLE IF NOT EXISTS ?PRE?_players(" +
                "playerUUID UUID, " +
                "playerName TEXT, " +
                "PRIMARY KEY (playerUUID))");

        //================================================================================
        // Table for containing all claims
        //================================================================================
        query.add("CREATE TABLE IF NOT EXISTS ?PRE?_claims(" +
                "claimUUID UUID, " +
                "playerUUID UUID REFERENCES ?PRE?_players(playerUUID), " +
                "claimName TEXT, " +
                "claimToast TEXT, " +
                "claimSize INT, " +
                "PRIMARY KEY (claimUUID))");

        //================================================================================
        // Table for containing all settings for claims
        //================================================================================
        query.add("CREATE TABLE IF NOT EXISTS ?PRE?_settings(" +
                "claimUUID UUID REFERENCES ?PRE?_claims(claimUUID), " +
                "settingName TEXT, " +
                "value JSON)");

        //================================================================================
        // Table for containing all chunks of claims
        //================================================================================
        query.add("CREATE TABLE IF NOT EXISTS ?PRE?_chunks(" +
                "claimUUID UUID REFERENCES ?PRE?_claims(claimUUID), " +
                "xChunkPos INT, " +
                "zChunkPos INT)");

        //================================================================================
        // Table for containing builders (Allowed Players) of claims
        //================================================================================
        query.add("CREATE TABLE IF NOT EXISTS ?PRE?_claimAccess(" +
                "claimUUID UUID REFERENCES ?PRE?_claims(claimUUID), " +
                "playerUUID UUID REFERENCES ?PRE?_players(playerUUID), " +
                "accessLevel TEXT)");

        //================================================================================
        // Table for the auction house
        //================================================================================
        query.add("CREATE TABLE IF NOT EXISTS ?PRE?_auctionHouse(" +
                "auctionUUID UUID, " +
                "claimUUID UUID REFERENCES ?PRE?_claims(claimUUID), " +
                "startingPrice INT, " +
                "AllowBID BOOL, " +
                "BIDNumber INT, " +
                "highestBID INT, " +
                "highestBIDPlayer UUID REFERENCES ?PRE?_players(PlayerUUID), " +
                "spectatePos TEXT, " +
                "soldToPlayer UUID REFERENCES ?PRE?_players(PlayerUUID), " +
                "forSale BOOL, " +
                "created DATE, " +
                "PRIMARY KEY (auctionUUID))");

        //================================================================================
        // Table for auction house bids
        //================================================================================
        query.add("CREATE TABLE IF NOT EXISTS ?PRE?_auctionHouse_BIDs(" +
                "playerUUID UUID REFERENCES ?PRE?_players(playerUUID), " +
                "auctionUUID UUID REFERENCES ?PRE?_auctionHouse(auctionUUID), " +
                "BIDAmount INT, " +
                "status TEXT, " +
                "BIDTimestamp DATE)");

        //================================================================================
        // Create default player with uid 0 codename: Nobody
        //================================================================================



        //Executes all the SQL commands in bunch
        this.executeQuerys(query);
        Plugin_IClaim.PLUGIN.getLogger().warning("Tables created successful");
        
        String f1 = "Nobody", f2 = "00000000-0000-0000-0000-000000000000";
        boolean hasContent = false;
        try
        {
            ResultSet result = this.executeQuery("SELECT * FROM ?PRE?_players WHERE (playerUUID=\"" + f2 + "\")");
            hasContent = result.last();
            result.close();
            Plugin_IClaim.PLUGIN.getLogger().warning("Tables warning: " + hasContent);
        }
        catch (SQLException | NullPointerException ignored) {/*TODO: warning for user*/}
        if (!hasContent)
        {
            this.executeQuery("INSERT INTO ?PRE?_players (playerName, playerUUID) VALUES (\"%s\", \"%s\")".formatted(f1, f2));
            Plugin_IClaim.PLUGIN.getLogger().warning("Tables created REALLY successful");
        }
        
        return true;
    }

    /**
     * Gets the player id from the Database
     * @param player the player in question
     * @return ID from database
     */
    public boolean playerExist(Player player)
    {
        try {
            String sql = "SELECT playerUUID FROM ?PRE?_players WHERE playerUUID='%s'".formatted(player.getUniqueId());
            ResultSet result = this.executeQuery(sql);
            result.last();
            if (result.getRow() > 1)
                throw new DBMultipleError("Mehrere players pro UUID existieren. Darf nicht passieren");
            UUID uuid = UUID.fromString(result.getString("playerUUID"));
            result.close();
            return (uuid.equals(player.getUniqueId()));
        }
        catch (SQLException | DBMultipleError ignored) { }

        Plugin_IClaim.DATABASE.executeQuery("INSERT INTO ?PRE?_players (playerName, playerUUID) VALUES (\"%s\", \"%s\")".formatted(player.getName(), player.getUniqueId().toString()));
        return false;
    }

    /**
     * compares the owner of a claim with the player
     * @param claimUUID
     * @param player
     * @return
     */
    public boolean ICM_CompareOwner(UUID claimUUID, Player player)
    {
        try
        {
            String sql = "SELECT playerUUID FROM ?PRE?_claims WHERE claimUUID='%s'".formatted(claimUUID.toString());
            ResultSet result = this.executeQuery(sql);
            result.last();
            if (result.getRow() > 1)
                throw new DBMultipleError("Mehrere players pro UUID existieren. Darf nicht passieren");
            UUID owner = UUID.fromString(result.getString("playerUUID"));
            result.close();
            return (owner.equals(player.getUniqueId()));
        }
        catch (SQLException | DBMultipleError ignored) { }
        return false;
    }

    /**
     * get all claims the player has with identifier and name
     * @param player
     * @return
     */
    public HashMap<UUID, String> getPlayerICMs(Player player) throws DBError
    {
        try
        {
            HashMap<UUID, String> cache = new HashMap<>();
            String sql = "SELECT * FROM ?PRE?_claims WHERE playerUUID='%s'".formatted(player.getUniqueId().toString());
            ResultSet result = this.executeQuery(sql);
            while (result.next())
            {
                UUID claimUUID = UUID.fromString(result.getString("claimUUID"));
                String claimName = result.getString("claimName");
                cache.putIfAbsent(claimUUID, claimName);
            }

            result.close();
            return cache;
        }
        catch (SQLException ignored) { }
        throw new DBError("There is no Claim Data for the Player");
    }

    public boolean ICM_Exists(UUID claimUUID)
    {
        //TODO: claimExists funktion
        return false;
    }

    public HashMap<UUID, String> getClaimNearPlayer() {return null;}
    public HashMap<UUID, String> getClaimsNearCoordinates() {return null;}

    public void addClaimToICM(int chunkX, int chunkZ, UUID claimUUID)
    {
        if (!ICM_Exists(claimUUID)) return;
        if (getClaim(chunkX, chunkZ) != null) return;
        String sql = "INSERT INTO ?PRE?_chunks (claimUUID, xChunkPos, zChunkPos) VALUES ('%s', %s, %s)".formatted(claimUUID, chunkX, chunkZ);
        Plugin_IClaim.DATABASE.executeQuery(sql);
    }

    public UUID getICMByPlayerClaim(Player player)
    {
        return getClaim(player.getChunk().getX(), player.getChunk().getZ());
    }

    public UUID getClaim(int chunkX, int chunkZ)
    {
        try
        {
            String sql = "SELECT * FROM ?PRE?_chunks WHERE xChunkPos=%s AND zChunkPos=%s".formatted(chunkX, chunkZ);
            ResultSet rs = Plugin_IClaim.DATABASE.executeQuery(sql);
            rs.last();
            System.out.println("Claims: " + rs.getRow());
            if (rs.getRow() > 0) throw new DBMultipleError("Mehrere claims haben einen Chunk!");
            //if (rs.getRow() == 0) cancelled = false;
            if (rs.getRow() < 0) System.out.println("Es ist ein Eintrag als claim vorhanden!");
            return UUID.fromString(rs.getString("claimUUID"));
        }
        catch (NullPointerException | SQLException | DBMultipleError ignored) {}
        return null;
    }

    public UUID createClaim(Player player, String name) {
        //TODO: createClaim funktion
        return null;
    }

    public void deleteClaim() {}

    //TODO weitere fälle hinzufügen
    //TODO spieler hinzufügen auch hier handlen
}