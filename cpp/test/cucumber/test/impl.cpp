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

#include <iostream>

#include "gtest/gtest.h"

#include "impl.hpp"

namespace RockPaperScissors {

const int ROCK_SCISSORS = (1 << ROCK) | (1 << SCISSORS);
const int PAPER_ROCK = (1 << PAPER) | (1 << ROCK);
const int SCISSORS_PAPER = (1 << SCISSORS) | (1 << PAPER);
const int DRAW = ROCK | PAPER | SCISSORS;

int winningMove(int bitMappedMoves) {
    switch (bitMappedMoves) {
        case ROCK_SCISSORS:
            return ROCK;
        case PAPER_ROCK:
            return PAPER;
        case SCISSORS_PAPER:
            return SCISSORS;
        default:
            return DRAW;
    }
}

Move stringToMove(const std::string& str) {
    if (str == "rock") {
        return ROCK;
    } else if (str == "paper") {
        return PAPER;
    } else if (str == "scissors") {
        return SCISSORS;
    } else throw std::runtime_error("Unrecognised move: " + str);
}

void nobody_moved(Context& context, cucumber::messages::pickle_step& step, const std::smatch& matches) {
    context.moves[ROCK].clear();
    context.moves[PAPER].clear();
    context.moves[SCISSORS].clear();
}

void record_move(Context& context, cucumber::messages::pickle_step& step, const std::smatch& matches) {
    context.moves[stringToMove(matches[2])].push_back(matches[1]);
}

// TODO: Support multiple winners?
void check_winner(Context& context, cucumber::messages::pickle_step& step, const std::smatch& matches) {
    int moves = 0;
    if (context.moves[ROCK].size() > 0) moves |= (1 << ROCK);
    if (context.moves[PAPER].size() > 0) moves |= (1 << PAPER);
    if (context.moves[SCISSORS].size() > 0) moves |= (1 << SCISSORS);

    int win = winningMove(moves);
    if (win != DRAW) {
        ASSERT_EQ(context.moves[win][0], matches[1]);
    } else ASSERT_EQ("draw", matches[1]);
}

cucumber_bdd::StepCollection<Context> steps = {
    {std::regex("Nobody has made a move"), &nobody_moved},
    {std::regex("([A-Za-z]+) plays (rock|paper|scissors)"), &record_move},
    {std::regex("([A-Za-z]+) wins"), &check_winner},
};

}  // namespace RockPaperScissors
