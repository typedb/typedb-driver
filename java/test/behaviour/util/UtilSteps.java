/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package com.typedb.driver.test.behaviour.util;

import com.typedb.driver.test.behaviour.config.Parameters;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.nio.file.Files;
import java.util.TimeZone;

import static com.typedb.driver.test.behaviour.connection.ConnectionStepsBase.fullPath;
import static com.typedb.driver.test.behaviour.util.Util.isEmpty;
import static com.typedb.driver.test.behaviour.util.Util.readFileToString;
import static com.typedb.driver.test.behaviour.util.Util.writeFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UtilSteps {

    @When("set time-zone: {word}")
    public void set_timezone(String value) {
        TimeZone.setDefault(TimeZone.getTimeZone(value));
    }

    @Then("wait {integer} seconds")
    public void wait_seconds(int seconds) throws InterruptedException {
        Thread.sleep(seconds * 1000L);
    }

    @Then("file\\({word}) {exists_or_doesnt}")
    public void file_exists(String name, Parameters.ExistsOrDoesnt existsOrDoesnt) {
        existsOrDoesnt.check(Files.exists(fullPath(name)));
    }

    @Then("file\\({word}) {is_or_not} empty")
    public void file_is_empty(String name, Parameters.IsOrNot isOrNot) {
        isOrNot.check(isEmpty(fullPath(name)));
    }

    @When("file\\({word}) write:")
    public void file_write(String name, String text) {
        writeFile(fullPath(name), text.strip());
    }
}
