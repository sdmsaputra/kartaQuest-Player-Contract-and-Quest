package com.minekarta.karta.playercontract.persistence

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.minekarta.karta.playercontract.domain.Reward
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * Utility for serializing and deserializing objects for database storage.
 */
object SerializationUtil {

    private val gson = Gson()

    /**
     * Serializes an object to a JSON string.
     */
    fun <T> toJson(obj: T): String = gson.toJson(obj)

    /**
     * Deserializes a JSON string back to a Reward object.
     */
    fun fromJsonToReward(json: String): Reward = gson.fromJson(json, Reward::class.java)

    /**
     * Serializes a list of ItemStacks into a Base64 encoded string for safer text storage.
     * Storing raw byte arrays can sometimes cause issues with character encoding.
     */
    fun serializeItemList(items: List<ItemStack>): ByteArray {
        ByteArrayOutputStream().use { bos ->
            BukkitObjectOutputStream(bos).use { oos ->
                oos.writeInt(items.size)
                items.forEach { oos.writeObject(it) }
                return bos.toByteArray()
            }
        }
    }

    /**
     * Deserializes a ByteArray back into a list of ItemStacks.
     */
    fun deserializeItemList(bytes: ByteArray): List<ItemStack> {
        if (bytes.isEmpty()) return emptyList()
        ByteArrayInputStream(bytes).use { bis ->
            BukkitObjectInputStream(bis).use { ois ->
                val size = ois.readInt()
                return List(size) { ois.readObject() as ItemStack }
            }
        }
    }
}
