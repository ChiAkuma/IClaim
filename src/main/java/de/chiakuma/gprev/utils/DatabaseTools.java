package de.chiakuma.gprev.utils;

import de.chiakuma.gprev.GriefPrevention;
import de.chiakuma.gprev.exceptions.DBError;
import de.chiakuma.gprev.exceptions.DBMultipleError;
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
            GriefPrevention.PLUGIN.getLogger().info("Database connection successfully");
        }
        catch (SQLException e)
        {
            GriefPrevention.PLUGIN.getLogger().warning("Database connection failed");
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
                "UUID UUID, " +
                "Name TEXT, " +
                "PRIMARY KEY (UUID))");

        //================================================================================
        // Table for containing all claims
        //================================================================================
        query.add("CREATE TABLE IF NOT EXISTS ?PRE?_ICMs(" +
                "UUID UUID, " +
                "playerUUID UUID REFERENCES ?PRE?_players(UUID), " +
                "Name TEXT, " +
                "Toast TEXT, " +
                "Size INT, " +
                "PRIMARY KEY (UUID))");

        //================================================================================
        // Table for containing all settings for claims
        //================================================================================
        query.add("CREATE TABLE IF NOT EXISTS ?PRE?_ICMsettings(" +
                "IcmUUID UUID REFERENCES ?PRE?_ICMs(UUID), " +
                "settingName TEXT, " +
                "value JSON)");

        //================================================================================
        // Table for containing all chunks of claims
        //================================================================================
        query.add("CREATE TABLE IF NOT EXISTS ?PRE?_claims(" +
                "IcmUUID UUID REFERENCES ?PRE?_ICMs(UUID), " +
                "xChunkPos INT, " +
                "zChunkPos INT)");

        //================================================================================
        // Table for containing builders (Allowed Players) of claims
        //================================================================================
        query.add("CREATE TABLE IF NOT EXISTS ?PRE?_ICMAccess(" +
                "claimUUID UUID REFERENCES ?PRE?_ICMs(UUID), " +
                "playerUUID UUID REFERENCES ?PRE?_players(UUID), " +
                "accessLevel TEXT)");

        //================================================================================
        // Table for the auction house
        //================================================================================
        query.add("CREATE TABLE IF NOT EXISTS ?PRE?_auctionHouse(" +
                "UUID UUID, " +
                "IcmUUID UUID REFERENCES ?PRE?_ICMs(UUID), " +
                "startingPrice INT, " +
                "AllowBID BOOL, " +
                "BIDNumber INT, " +
                "highestBID INT, " +
                "highestBIDPlayer UUID REFERENCES ?PRE?_players(UUID), " +
                "spectatePos TEXT, " +
                "soldToPlayer UUID REFERENCES ?PRE?_players(UUID), " +
                "forSale BOOL, " +
                "created DATE, " +
                "PRIMARY KEY (UUID))");

        //================================================================================
        // Table for auction house bids
        //================================================================================
        query.add("CREATE TABLE IF NOT EXISTS ?PRE?_auctionHouse_BIDs(" +
                "playerUUID UUID REFERENCES ?PRE?_players(UUID), " +
                "auctionUUID UUID REFERENCES ?PRE?_auctionHouse(UUID), " +
                "BIDAmount INT, " +
                "status TEXT, " +
                "BIDTimestamp DATE)");

        //================================================================================
        // Create default player with uid 0 codename: Nobody
        //================================================================================



        //Executes all the SQL commands in bunch
        this.executeQuerys(query);
        GriefPrevention.PLUGIN.getLogger().warning("Tables created successful");
        
        String f1 = "Nobody", f2 = "00000000-0000-0000-0000-000000000000";
        boolean hasContent = false;
        try
        {
            ResultSet result = this.executeQuery("SELECT * FROM ?PRE?_players WHERE (UUID=\"" + f2 + "\")");
            hasContent = result.last();
            result.close();
            GriefPrevention.PLUGIN.getLogger().warning("Tables warning: " + hasContent);
        }
        catch (SQLException | NullPointerException ignored) {/*TODO: warning for user*/}
        if (!hasContent)
        {
            this.executeQuery("INSERT INTO ?PRE?_players (Name, UUID) VALUES (\"%s\", \"%s\")".formatted(f1, f2));
            GriefPrevention.PLUGIN.getLogger().warning("Tables created REALLY successful");
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
            String sql = "SELECT UUID FROM ?PRE?_players WHERE UUID='%s'".formatted(player.getUniqueId());
            ResultSet result = this.executeQuery(sql);
            result.last();
            if (result.getRow() > 1)
                throw new DBMultipleError("Mehrere players pro UUID existieren. Darf nicht passieren");
            UUID uuid = UUID.fromString(result.getString("UUID"));
            result.close();
            return (uuid.equals(player.getUniqueId()));
        }
        catch (SQLException | DBMultipleError ignored) { }
        GriefPrevention.DATABASE.executeQuery("INSERT INTO ?PRE?_players (Name, UUID) VALUES (\"%s\", \"%s\")".formatted(player.getName(), player.getUniqueId().toString()));
        return false;
    }

    /**
     * compares the owner of a claim with the player
     * @param IcmUUID
     * @param player
     * @return
     */
    public boolean ICM_CompareOwner(UUID IcmUUID, Player player)
    {
        try
        {
            String sql = "SELECT playerUUID FROM ?PRE?_ICMs WHERE UUID='%s'".formatted(IcmUUID.toString());
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
            String sql = "SELECT * FROM ?PRE?_ICMs WHERE playerUUID='%s'".formatted(player.getUniqueId().toString());
            ResultSet result = this.executeQuery(sql);
            while (result.next())
            {
                UUID claimUUID = UUID.fromString(result.getString("UUID"));
                String claimName = result.getString("Name");
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
        //checks if icm exists or not
        try
        {
            String sql = "SELECT * FROM ?PRE?_ICMs WHERE UUID='%s'".formatted(claimUUID);
            ResultSet rs = GriefPrevention.DATABASE.executeQuery(sql);
            rs.last();
            if (rs.getRow() == 0) return false;
            if (rs.getRow() > 1) throw new DBMultipleError("ICM UUID count is over 1"); //Here is an ERROR
            rs.close();
            return true;
        }
        catch (DBMultipleError | SQLException ignored) {}
        return false;
    }

    public HashMap<UUID, String> getClaimNearPlayer() {return null;}
    public HashMap<UUID, String> getClaimsNearCoordinates() {return null;}

    public boolean addClaimToICM(int chunkX, int chunkZ, UUID IcmUUID)
    {
        System.out.println("ICM uuid is: " + IcmUUID);
        if (!ICM_Exists(IcmUUID)) return false;
        System.out.println("icm exists");
        if (getClaim(chunkX, chunkZ) != null) return false;
        System.out.println("claim is not used");
        String sql = "INSERT INTO ?PRE?_claims (IcmUUID, xChunkPos, zChunkPos) VALUES ('%s', %s, %s)".formatted(IcmUUID, chunkX, chunkZ);
        if (GriefPrevention.DATABASE.executeQuery(sql) == null) return false;
        System.out.println("first query works");

        String sql1 = "UPDATE ?PRE?_ICMs SET Size = Size + 1 WHERE UUID = '%s'".formatted(IcmUUID);
        if (GriefPrevention.DATABASE.executeQuery(sql1) == null) return false;
        System.out.println("Second query works");
        return true;
    }

    public ResultSet getICMByPlayerClaim(Player player)
    {
        return getClaim(player.getChunk().getX(), player.getChunk().getZ());
    }

    public ResultSet getClaim(int chunkX, int chunkZ)
    {
        try
        {
            String sql = "SELECT * FROM ?PRE?_claims WHERE xChunkPos=%s AND zChunkPos=%s".formatted(chunkX, chunkZ);
            ResultSet rs = GriefPrevention.DATABASE.executeQuery(sql);
            rs.last();
            System.out.println("Claims: " + rs.getRow());
            if (rs.getRow() > 1) throw new DBMultipleError("Mehrere claims haben einen Chunk!");
            if (rs.getRow() == 0) return null;
            return rs;
        }
        catch (NullPointerException | SQLException | DBMultipleError ignored) {}
        return null;
    }

    public UUID createICM(Player player, String name) {
        //TODO: createClaim funktion
        UUID IcmUUID = UUID.randomUUID();
        String sql = "INSERT INTO ?PRE?_ICMs (UUID, playerUUID, Name, Toast, Size) VALUES ('%s', '%s', '%s', '', 0)".formatted(IcmUUID, player.getUniqueId(),  name);
        if (GriefPrevention.DATABASE.executeQuery(sql) == null) return null;
        return IcmUUID;
    }

    public void deleteICM() {}

    //TODO weitere fälle hinzufügen
    //TODO spieler hinzufügen auch hier handlen
}