package de.chiakuma.gprev.utils;

import org.bukkit.entity.Player;

public class SystemMessages
{
    //TODO: Sprachen hinzufügen und pro spieler lokalisieren. Wird aber auch ein allgemeines projekt ;)
    public static void SQLError(Player player)
    {
        player.sendMessage("-------------------------------IClaim System---------------------------------");
        player.sendMessage("Ein Fehler ist während des Ausführens des Commands aufgetreten");
        player.sendMessage("Falls du diese Nachricht öfter siehst benachrichtige einen Server Admin");
        player.sendMessage("-----------------------------------------------------------------------------");
    }

    public static void ClaimCommandUsage(Player player)
    {
        player.sendMessage("-------------------------------IClaim System---------------------------------");
        player.sendMessage("Benutze den Command so: /iclaim <create|edit|addchunk|removechunk|list>");
        player.sendMessage("-----------------------------------------------------------------------------");
    }

    public static void ChunkTaken(Player player)
    {
        player.sendMessage("-------------------------------IClaim System---------------------------------");
        player.sendMessage("An diesem Chunk befindet sich bereits ein Chunk. Du kannst kein neues erstellen!");
        player.sendMessage("-----------------------------------------------------------------------------");
    }
}
