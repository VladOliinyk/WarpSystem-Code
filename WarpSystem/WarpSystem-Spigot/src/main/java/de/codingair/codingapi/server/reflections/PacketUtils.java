package de.codingair.codingapi.server.reflections;

import de.codingair.codingapi.server.specification.Version;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class PacketUtils {
    // Static fields for NMS classes
    public static Class<?> CraftPlayerClass;
    public static Class<?> EntityPlayerClass;
    public static Class<?> PlayerConnectionClass;
    public static Class<?> WorldServerClass;
    public static Class<?> MinecraftServerClass;
    public static Class<?> NetworkManagerClass;
    public static Class<?> IChatBaseComponentClass;
    
    // Component Serializer - may be null on 1.21.11+
    public static Class<?> ChatSerializer;
    
    static {
        // Initialize core NMS classes (these should always exist)
        try {
            CraftPlayerClass = IReflection.getClass("org.bukkit.craftbukkit.", "entity.CraftPlayer");
            EntityPlayerClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "EntityPlayer");
            PlayerConnectionClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PlayerConnection");
            WorldServerClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "WorldServer");
            MinecraftServerClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "MinecraftServer");
            NetworkManagerClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "NetworkManager");
            IChatBaseComponentClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "IChatBaseComponent");
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Failed to initialize core NMS classes: " + e.getMessage());
        }
        
        // Patch: Wrap Component$Serializer loading in try-catch
        // On Paper 1.21.11+, this class doesn't exist - set to null instead of crashing
        try {
            ChatSerializer = IReflection.getClass("net.minecraft.network.chat.Component$Serializer");
        } catch (RuntimeException e) {
            // On 1.21.11+, this class doesn't exist - set to null instead of crashing
            // IReflection.getClass throws RuntimeException, not ClassNotFoundException
            ChatSerializer = null;
            // Don't throw exception - allow PacketUtils to load successfully
        }
    }
    
    /**
     * Gets the Bukkit entity from an NMS entity
     */
    public static Object getBukkitEntity(Object nmsEntity) {
        try {
            IReflection.MethodAccessor getBukkitEntity = IReflection.getMethod(EntityPlayerClass, "getBukkitEntity");
            return getBukkitEntity.invoke(nmsEntity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get Bukkit entity", e);
        }
    }
    
    /**
     * Gets the PlayerConnection from a Player
     */
    public static Object getPlayerConnection(Player player) {
        try {
            Object craftPlayer = CraftPlayerClass.cast(player);
            IReflection.FieldAccessor<?> handleField = IReflection.getField(CraftPlayerClass, EntityPlayerClass, 0);
            Object entityPlayer = handleField.get(craftPlayer);
            IReflection.FieldAccessor<?> connectionField = IReflection.getField(EntityPlayerClass, PlayerConnectionClass, 0);
            return connectionField.get(entityPlayer);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get PlayerConnection", e);
        }
    }
    
    /**
     * Gets the MinecraftServer instance
     */
    public static Object getMinecraftServer() {
        try {
            IReflection.MethodAccessor getServer = IReflection.getMethod(Bukkit.getServer().getClass(), "getServer");
            return getServer.invoke(Bukkit.getServer());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get MinecraftServer", e);
        }
    }
    
    /**
     * Gets the WorldServer from a Bukkit World
     */
    public static Object getWorldServer(World world) {
        try {
            IReflection.MethodAccessor getHandle = IReflection.getMethod(world.getClass(), "getHandle");
            return getHandle.invoke(world);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get WorldServer", e);
        }
    }
    
    /**
     * Creates an IChatBaseComponent from a string
     * Fallback logic for Paper 1.21.11+ where Component$Serializer doesn't exist
     */
    public static Object getRawIChatBaseComponent(String text) {
        if (ChatSerializer == null) {
            // Fallback for Paper 1.21.11+ where Component$Serializer doesn't exist
            // Option 1: Use CraftBukkit's CraftChatMessage utility (RECOMMENDED)
            try {
                // CraftChatMessage is available in CraftBukkit and handles chat component creation
                // Path: org.bukkit.craftbukkit.<version>.util.CraftChatMessage
                String packageName = Bukkit.getServer().getClass().getPackage().getName();
                String nmsVersion = packageName.substring(packageName.lastIndexOf('.') + 1);
                String craftChatMessageClass = "org.bukkit.craftbukkit." + nmsVersion + ".util.CraftChatMessage";
                
                Class<?> craftChatMessage = Class.forName(craftChatMessageClass);
                
                // Try fromString first (simpler, for plain text)
                try {
                    java.lang.reflect.Method fromString = craftChatMessage.getMethod("fromString", String.class);
                    return fromString.invoke(null, text);
                } catch (NoSuchMethodException e) {
                    // Fallback to fromJSON if fromString doesn't exist
                    java.lang.reflect.Method fromJSON = craftChatMessage.getMethod("fromJSON", String.class);
                    String json = "{\"text\":\"" + escapeJson(text) + "\"}";
                    return fromJSON.invoke(null, json);
                }
            } catch (Exception e) {
                // Option 2: Create simple text component via IChatBaseComponent constructor
                try {
                    // Try to find ChatComponentText constructor
                    Class<?> chatComponentTextClass = IReflection.getClass(
                        IReflection.ServerPacket.MINECRAFT_PACKAGE, 
                        "ChatComponentText"
                    );
                    java.lang.reflect.Constructor<?> constructor = chatComponentTextClass.getConstructor(String.class);
                    return constructor.newInstance(text);
                } catch (Exception e2) {
                    // Option 3: Last resort - use IChatBaseComponent static method if available
                    try {
                        // Some versions have a static method to create text components
                        IReflection.MethodAccessor createText = IReflection.getMethod(
                            IChatBaseComponentClass, 
                            Version.choose("a", 17, "b"), // Common obfuscated method names
                            null,
                            new Class[]{String.class}
                        );
                        return createText.invoke(null, text);
                    } catch (Exception e3) {
                        throw new RuntimeException("Failed to create IChatBaseComponent: Component$Serializer not available and fallbacks failed", e3);
                    }
                }
            }
        }
        
        // Original implementation using ChatSerializer
        try {
            // Use ChatSerializer to deserialize JSON string
            IReflection.MethodAccessor fromJson = IReflection.getMethod(ChatSerializer, "fromJson", String.class, new Class[0]);
            String json = "{\"text\":\"" + escapeJson(text) + "\"}";
            return fromJson.invoke(null, json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create IChatBaseComponent using ChatSerializer", e);
        }
    }
    
    /**
     * Sends a packet to a player
     */
    public static void sendPacket(Player player, Object packet) {
        try {
            Object playerConnection = getPlayerConnection(player);
            IReflection.MethodAccessor sendPacket = IReflection.getMethod(PlayerConnectionClass, "sendPacket", null, new Class[]{packet.getClass().getSuperclass()});
            sendPacket.invoke(playerConnection, packet);
        } catch (Exception e) {
            // Try alternative method name
            try {
                Object playerConnection = getPlayerConnection(player);
                IReflection.MethodAccessor sendPacket = IReflection.getMethod(PlayerConnectionClass, Version.choose("sendPacket", 17, "a"), null, new Class[]{packet.getClass().getSuperclass()});
                sendPacket.invoke(playerConnection, packet);
            } catch (Exception e2) {
                throw new RuntimeException("Failed to send packet", e2);
            }
        }
    }
    
    /**
     * Escapes JSON special characters
     */
    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r");
    }
}
