package com.coffee4j;

/**
 * A body of a response to a Coffee4j API request.
 *
 * @author Logan Kulinski, lbkulinski@gmail.com
 * @version April 23, 2022
 * @param status the status of this body
 * @param content the content of this body
 * @param <T> the type of the content of this body
 */
public record Body<T>(Status status, T content) {
    /**
     * A body's status.
     */
    public enum Status {
        /**
         * The singleton instance representing the success status.
         */
        SUCCESS,

        /**
         * The singleton instance representing the error status.
         */
        ERROR
    } //Status

    /**
     * Returns a successful body using the specified content.
     *
     * @param content the content to be used in the operation
     * @return a successful body using the specified content
     * @param <T> the type of the specified content
     */
    public static <T> Body<T> success(T content) {
        return new Body<>(Status.SUCCESS, content);
    } //success

    /**
     * Returns an error body using the specified content.
     *
     * @param content the content to be used in the operation
     * @return an error body using the specified content
     * @param <T> the type of the specified content
     */
    public static <T> Body<T> error(T content) {
        return new Body<>(Status.ERROR, content);
    } //error
}