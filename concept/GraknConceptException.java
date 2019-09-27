/*
 * GRAKN.AI - THE KNOWLEDGE GRAPH
 * Copyright (C) 2019 Grakn Labs Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package grakn.client.concept;

public class GraknConceptException extends RuntimeException {
    final static String INVALID_OBJECT_TYPE = "The concept [%s] is not of type [%s]";
    final static String VARIABLE_DOES_NOT_EXIST = "the variable [%s] does not exist";

    private GraknConceptException(String error) {
        super(error);
    }

    public String getName() {
        return this.getClass().getName();
    }

    public static GraknConceptException create(String error) {
        return new GraknConceptException(error);
    }

    /**
     * Thrown when casting Grakn concepts/answers incorrectly.
     */
    public static GraknConceptException invalidCasting(Object concept, Class type) {
        return GraknConceptException.create(String.format(INVALID_OBJECT_TYPE,concept, type));
    }

    public static GraknConceptException variableDoesNotExist(String var) {
        return new GraknConceptException(String.format(VARIABLE_DOES_NOT_EXIST, var));
    }
}
