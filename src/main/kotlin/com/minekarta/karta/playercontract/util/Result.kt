package com.minekarta.karta.playercontract.util

/**
 * A generic sealed class for representing the result of an operation that can either succeed or fail.
 * @param T The type of the success value.
 * @param E The type of the error value.
 */
sealed class Result<out T, out E> {
    /**
     * Represents a successful result.
     * @param value The success value.
     */
    data class Success<out T>(val value: T) : Result<T, Nothing>()

    /**
     * Represents a failure result.
     * @param error The error value.
     */
    data class Failure<out E>(val error: E) : Result<Nothing, E>()

    companion object {
        /**
         * Creates a new success result.
         * @param value The success value.
         */
        fun <T> success(value: T): Result<T, Nothing> = Success(value)

        /**
         * Creates a new failure result.
         * @param error The error value.
         */
        fun <E> failure(error: E): Result<Nothing, E> = Failure(error)
    }
}
