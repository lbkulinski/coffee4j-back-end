/*
 * MIT License
 *
 * Copyright (c) 2022 Logan Kulinski
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.coffee4j;

/**
 * A body of a response to a Coffee4j API request.
 *
 * @author Logan Kulinski, rashes_lineage02@icloud.com
 * @version July 11, 2022
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