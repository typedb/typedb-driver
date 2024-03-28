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

#include <cstdlib>

#include "common.hpp"
#include "concept_utils.hpp"
#include "steps.hpp"
#include "utils.hpp"

namespace TypeDB::BDD {

using namespace cucumber::messages;

cucumber_bdd::StepCollection<Context> attributeSteps = {
    BDD_STEP("attribute\\((\\S+)\\) get instances contain: \\$(\\S+)", {
        ASSERT_TRUE(containsInstance(collect(attrType(context, matches[1].str())->getInstances(context.transaction())), context.vars[matches[2].str()].get()));
    }),
    BDD_STEP("attribute \\$(\\S+) has value type: (\\w+)", {
        ASSERT_EQ(parseValueType(matches[2].str()), context.vars[matches[1].str()]->asAttribute()->getType()->getValueType());
    }),

    // Create instance
    BDD_STEP("attribute\\((\\S+)\\) as\\(boolean\\) put: (true|false); throws exception", {
        DRIVER_THROWS("", {
            attrType(context, matches[1].str())->put(context.transaction(), parseBoolean(matches[2].str())).get();
        });
    }),
    BDD_STEP("attribute\\((\\S+)\\) as\\(long\\) put: (\\d+); throws exception", {
        DRIVER_THROWS("", {
            attrType(context, matches[1].str())->put(context.transaction(), parseBoolean(matches[2].str())).get();
        });
    }),
    BDD_STEP("attribute\\((\\S+)\\) as\\(double\\) put: ([0-9\\.]+); throws exception", {
        DRIVER_THROWS("", {
            attrType(context, matches[1].str())->put(context.transaction(), parseBoolean(matches[2].str())).get();
        });
    }),
    BDD_STEP("attribute\\((\\S+)\\) as\\(string\\) put: (.*); throws exception", {
        DRIVER_THROWS("", {
            attrType(context, matches[1].str())->put(context.transaction(), parseBoolean(matches[2].str())).get();
        });
    }),
    BDD_STEP("attribute\\((\\S+)\\) as\\(datetime\\) put: ([0-9TZ:\\- ]+); throws exception", {
        DRIVER_THROWS("", {
            attrType(context, matches[1].str())->put(context.transaction(), parseBoolean(matches[2].str())).get();
        });
    }),

    BDD_STEP("\\$(.+) = attribute\\((.+)\\) as\\(boolean\\) put: (true|false)", {
        context.vars[matches[1].str()] = attrType(context, matches[2].str())->put(context.transaction(), parseBoolean(matches[3].str())).get();
    }),
    BDD_STEP("\\$(\\w+) = attribute\\((\\S+)\\) as\\(long\\) put: (\\d+)", {
        context.vars[matches[1].str()] = attrType(context, matches[2].str())->put(context.transaction(), parseLong(matches[3].str())).get();
    }),
    BDD_STEP("\\$(\\w+) = attribute\\((\\S+)\\) as\\(double\\) put: ([0-9\\.]+)", {
        context.vars[matches[1].str()] = attrType(context, matches[2].str())->put(context.transaction(), parseDouble(matches[3].str())).get();
    }),
    BDD_STEP("\\$(\\w+) = attribute\\((\\S+)\\) as\\(string\\) put: (.*)", {
        context.vars[matches[1].str()] = attrType(context, matches[2].str())->put(context.transaction(), matches[3].str()).get();
    }),
    BDD_STEP("\\$(\\w+) = attribute\\((\\S+)\\) as\\(datetime\\) put: ([0-9TZ:\\- ]+)", {
        context.vars[matches[1].str()] = attrType(context, matches[2].str())->put(context.transaction(), parseDateTime(matches[3].str())).get();
    }),

    // Read instance
    BDD_STEP("attribute \\$(\\w+) has boolean value: (true|false)", {
        ASSERT_EQ(parseBoolean(matches[2].str()), context.vars[matches[1].str()]->asAttribute()->getValue()->asBoolean());
    }),
    BDD_STEP("attribute \\$(\\w+) has long value: (\\d+)", {
        ASSERT_EQ(parseLong(matches[2].str()), context.vars[matches[1].str()]->asAttribute()->getValue()->asLong());
    }),
    BDD_STEP("attribute \\$(\\w+) has double value: ([0-9\\.]+)", {
        ASSERT_EQ(parseDouble(matches[2].str()), context.vars[matches[1].str()]->asAttribute()->getValue()->asDouble());
    }),
    BDD_STEP("attribute \\$(\\w+) has string value: (.*)", {
        ASSERT_EQ(matches[2].str(), context.vars[matches[1].str()]->asAttribute()->getValue()->asString());
    }),
    BDD_STEP("attribute \\$(\\w+) has datetime value: ([0-9TZ:\\- ]+)", {
        ASSERT_EQ(parseDateTime(matches[2].str()), context.vars[matches[1].str()]->asAttribute()->getValue()->asDateTime());
    }),

    BDD_STEP("\\$(\\w+) = attribute\\((\\S+)\\) as\\(boolean\\) get: (true|false)", {
        context.vars[matches[1].str()] = attrType(context, matches[2].str())->get(context.transaction(), parseBoolean(matches[3].str())).get();
    }),
    BDD_STEP("\\$(\\w+) = attribute\\((\\S+)\\) as\\(long\\) get: (\\d+)", {
        context.vars[matches[1].str()] = attrType(context, matches[2].str())->get(context.transaction(), parseLong(matches[3].str())).get();
    }),
    BDD_STEP("\\$(\\w+) = attribute\\((\\S+)\\) as\\(double\\) get: ([0-9\\.]+)", {
        context.vars[matches[1].str()] = attrType(context, matches[2].str())->get(context.transaction(), parseDouble(matches[3].str())).get();
    }),
    BDD_STEP("\\$(\\w+) = attribute\\((\\S+)\\) as\\(string\\) get: (.*)", {
        context.vars[matches[1].str()] = attrType(context, matches[2].str())->get(context.transaction(), matches[3].str()).get();
    }),
    BDD_STEP("\\$(\\w+) = attribute\\((\\S+)\\) as\\(datetime\\) get: ([0-9TZ:\\- ]+)", {
        context.vars[matches[1].str()] = attrType(context, matches[2].str())->get(context.transaction(), parseDateTime(matches[3].str())).get();
    }),

    // Owners
    BDD_STEP("attribute \\$(\\S+) get owners contain: \\$(\\S+)", {
        auto owners = collect(context.vars[matches[1].str()]->asAttribute()->getOwners(context.transaction()));
        ASSERT_TRUE(containsInstance(owners, context.vars[matches[2].str()].get()));
    }),
    BDD_STEP("attribute \\$(\\S+) get owners do not contain: \\$(\\S+)", {
        auto owners = collect(context.vars[matches[1].str()]->asAttribute()->getOwners(context.transaction()));
        ASSERT_FALSE(containsInstance(owners, context.vars[matches[2].str()].get()));
    }),
};

}  // namespace TypeDB::BDD
