package de.codingair.warpsystem.spigot.features.beta.functions;

import de.codingair.warpsystem.base.utils.Manager;

public interface Beta extends Manager {
    boolean active();
    String getFinalConfigTag();
}
