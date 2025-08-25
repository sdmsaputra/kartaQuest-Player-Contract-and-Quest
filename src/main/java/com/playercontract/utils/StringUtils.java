package com.playercontract.utils;

import org.bukkit.Material;

public class StringUtils {

    public static String formatItemName(Material material) {
        if (material == null) {
            return "";
        }
        String name = material.name().replace('_', ' ').toLowerCase();
        StringBuilder formattedName = new StringBuilder();
        for (String word : name.split(" ")) {
            if (!word.isEmpty()) {
                formattedName.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        return formattedName.toString().trim();
    }
}
