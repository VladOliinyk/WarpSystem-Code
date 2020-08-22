package de.codingair.warpsystem.spigot.features.beta;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.codingapi.files.loader.UTFConfig;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.setupassistant.annotations.AvailableForSetupAssistant;
import de.codingair.warpsystem.spigot.base.setupassistant.annotations.Function;
import de.codingair.warpsystem.spigot.features.beta.functions.Beta;
import de.codingair.warpsystem.spigot.features.beta.functions.TeleportInterceptions;
import de.codingair.warpsystem.utils.Manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@AvailableForSetupAssistant(type = "Beta", config = "Config")
@Function(name = "Participate", description = "The beta area is there to make updates as easy as possible. Here, you decide whether you want to participate in beta functions and help to test new features.\n\n" +
        "§c§lWarning: §fBeta stuff is §cnot 100% tested §rand §cmay cause issues§r. Please visit my §bDiscord §ror DM me with issues! ", defaultValue = "false", configPath = "WarpSystem.Beta.Participate", clazz = Boolean.class, since = "v4.2.11")
@Function(name = "Teleport interceptions", description = "Cancels teleports from other plugins and replaces them with own teleport procedures to add teleport delays, particles, sounds and potion effects", configPath = "WarpSystem.Beta.Functions.Teleport_Interceptions", defaultValue = "false", clazz = Boolean.class, since = "v4.2.11")
public class BetaManager implements Manager {
    private static final HashMap<String, Beta> BETA = new HashMap<>();

    static {
        BETA.put("Teleport_Interceptions", new TeleportInterceptions());
    }

    @Override
    public boolean load(boolean loader) {
        Set<String> functions = new HashSet<>();

        for(String key : WarpSystem.getInstance().getOldConfig().getKeys(true)) {
            if(key.startsWith(".")) key = key.substring(1);
            if(key.startsWith("WarpSystem.Beta.Functions.")) functions.add(key.substring(26));
        }

        functions.remove("PLACEHOLDER");

        ConfigFile file = WarpSystem.getInstance().getFileManager().getFile("Config");
        UTFConfig config = file.getConfig();
        boolean participate = config.getBoolean("WarpSystem.Beta.Participate");

        for(String function : functions) {
            if(!config.contains("WarpSystem.Beta.Functions." + function)) {
                //moved from beta to official feature
                //activate only if already participating on BETA and function was enabled during BETA stage
                config.set(BETA.get(function).getFinalConfigTag(), participate && config.getBoolean("WarpSystem.Beta.Functions." + function));
            }
        }

        file.saveConfig();
        functions.clear();

        if(!participate) return true;

        WarpSystem.log("  > Loading BETA features - Thank you for your help!");

        //enable beta features
        boolean success = true;

        int amount = 0, activated = 0;

        for(Map.Entry<String, Beta> e : BETA.entrySet()) {
            if(!e.getValue().active()) continue;
            amount++;

            if(config.getBoolean("WarpSystem.Beta.Functions." + e.getKey())) {
                activated++;

                if(!e.getValue().load(loader)) success = false;
            }
        }

        WarpSystem.log("    > " + activated + "/" + amount + " feature(s) activated");

        return success;
    }

    @Override
    public void save(boolean saver) {
        for(Beta value : BETA.values()) {
            value.save(saver);
        }
    }

    @Override
    public void destroy() {
        for(Beta value : BETA.values()) {
            value.destroy();
        }
    }
}
