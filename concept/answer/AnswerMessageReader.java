/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package grakn.client.concept.answer;

import com.google.protobuf.ByteString;
import grakn.client.common.exception.GraknException;
import grakn.protocol.AnswerProto;

import java.text.NumberFormat;
import java.text.ParseException;

import static grakn.common.collection.Bytes.bytesToHexString;

abstract class AnswerMessageReader {

    static String iid(final ByteString res) {
        return bytesToHexString(res.toByteArray());
    }

    static Number number(final AnswerProto.Number res) {
        try {
            return NumberFormat.getInstance().parse(res.getValue());
        } catch (ParseException e) {
            throw new GraknException(e);
        }
    }
}
