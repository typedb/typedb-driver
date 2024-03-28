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

#pragma once
#include <future>
#include <vector>

#include "typedb_driver.hpp"

#include "cucumber/messages/pickle_table.hpp"
#define THROW_ILLEGAL_STATE(SOMESTRING) \
    { throw std::runtime_error("ILLEGAL STATE! " + std::string(SOMESTRING)); }

namespace TypeDB::BDD {

// BDD value parsing
bool parseBoolean(const std::string& str);
int64_t parseLong(const std::string& str);
double parseDouble(const std::string& str);
TypeDB::DateTime parseDateTime(const std::string& s);
TypeDB::ValueType parseValueType(const std::string& str);
std::unique_ptr<TypeDB::Value> parseValueFromString(ValueType valueType, const std::string& str);

std::vector<TypeDB::Annotation> parseAnnotation(const std::string& str);
TypeDB::Transitivity parseTransitivity(const std::string& str);
TypeDB::TransactionType parseTransactionType(const std::string& str);

bool compareStringWithDouble(std::string& first, double second);
bool compareStringWithDateTime(std::string&, TypeDB::DateTime);

template <typename _1, typename _2, typename _3, typename T>
std::vector<T> collect(Iterable<_1, _2, T, _3> it) {
    std::vector<T> v;
    for (auto& x : it)
        v.push_back(std::move(x));
    return v;
}

std::pair<std::string, std::string> split(std::string str, std::string delimiter);


// Utils for test steps with tables
bool checkContains(const cucumber::messages::pickle_table& table, const std::vector<std::string>& toCheckList);
bool checkNotContains(const cucumber::messages::pickle_table& table, const std::vector<std::string>& toCheckList);
bool checkEqual(const cucumber::messages::pickle_table& table, const std::vector<std::string>& toCheckList);

template <typename T1>
bool containsInstance(const std::vector<std::unique_ptr<T1>>& instanceList, Concept* instanceToFind) {
    return std::any_of(instanceList.begin(), instanceList.end(), [&](const std::unique_ptr<T1>& x) { return Concept::equals(x.get(), instanceToFind); });
}

template <typename _1, typename _2, typename _3, typename IN, typename OUT>
std::vector<OUT> transform(TypeDB::Iterable<_1, _2, IN, _3> it, std::function<OUT(const IN&)> fn) {
    std::vector<OUT> v;
    for (auto& in : it) {
        v.push_back(fn(in));
    }
    return v;
}


template <typename _1, typename _2, typename _3, typename IN, typename OUT>
std::vector<OUT> transform(TypeDB::Iterable<_1, _2, IN, _3> it, OUT (*fn)(const IN&)) {
    std::vector<OUT> v;
    for (auto& in : it)
        v.push_back(fn(in));
    return v;
}

//
template <typename T>
struct zipped {
    cucumber::messages::pickle_table_row* row;
    T* obj;
};

template <typename T>
std::vector<zipped<T>> zip(std::vector<cucumber::messages::pickle_table_row>& rows, std::vector<T>& objs) {  // Important that these are by reference.
    assert(rows.size() == objs.size());
    std::vector<zipped<T>> z;
    for (size_t i = 0; i < rows.size(); i++) {
        z.push_back(zipped<T>{&rows[i], &objs[i]});
    }
    return z;
}

template <typename A1>
void foreach_serial(std::vector<A1>& args, std::function<void(A1*)> fn) {
    for (auto& arg : args) {
        fn(&arg);
    }
}

template <typename T, typename A1>
std::vector<T> apply_serial(std::vector<A1>& args, std::function<T(A1*)> fn) {
    std::vector<T> results;
    for (auto& arg : args)
        results.push_back(fn(&arg));
    return results;
}

template <typename A1>
void foreach_parallel(std::vector<A1>& args, std::function<void(A1*)> fn) {
    std::vector<std::future<void>> futures;
    for (A1& arg : args) {
        futures.push_back(std::async(std::launch::async, fn, &arg));
    }
    std::for_each(futures.begin(), futures.end(), [](std::future<void>& f) { f.wait(); });
}

template <typename T, typename A1>
std::vector<T> apply_parallel(std::vector<A1>& args, std::function<T(A1*)> fn) {
    std::vector<std::future<T>> futures;
    for (A1& arg : args) {
        futures.push_back(std::async(std::launch::async, fn, &arg));
    }
    std::vector<T> results;
    std::transform(futures.begin(), futures.end(), std::back_inserter(results), [](std::future<T>& f) { return f.get(); });
    return results;
}

}  // namespace TypeDB::BDD
