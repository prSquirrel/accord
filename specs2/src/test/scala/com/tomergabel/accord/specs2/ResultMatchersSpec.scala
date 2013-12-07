/*
  Copyright 2013 Tomer Gabel

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package com.tomergabel.accord.specs2

import org.specs2.mutable.Specification
import com.tomergabel.accord._
import org.specs2.matcher.{MatchResult, Matcher}
import com.tomergabel.accord.GroupViolation
import com.tomergabel.accord.RuleViolation

class ResultMatchersSpec extends Specification with ResultMatchers {

  "RuleViolationMatcher" should {

    val sampleViolation = RuleViolation( "value", "constraint", "description" )

    "correctly match a rule violation based on value" in {
      val matchRule = RuleViolationMatcher( value = "value" )
      sampleViolation should matchRule
    }

    "correctly match a rule violation based on constraint" in {
      val matchRule = RuleViolationMatcher( constraint = "constraint" )
      sampleViolation should matchRule
    }

    "correctly match a rule violation based on description" in {
      val matchRule = RuleViolationMatcher( description = "description" )
      sampleViolation should matchRule
    }

    "fail to match a non-matching rule violation" in {
      val matchRule = RuleViolationMatcher( value = "incorrect" )
      sampleViolation should not( matchRule )
    }
  }

  "GroupViolationMatcher" should {

    val sampleRule = RuleViolation( "value", "constraint", "description" )
    val sampleGroup = GroupViolation( value = "group", constraint = "violation", description = "ftw", children = Seq( sampleRule ) )

    "correctly match a group violation based on value" in {
      val matchRule = GroupViolationMatcher( value = "group" )
      sampleGroup should matchRule
    }

    "correctly match a rule violation based on constraint" in {
      val matchRule = GroupViolationMatcher( constraint = "violation" )
      sampleGroup should matchRule
    }

    "correctly match a rule violation based on description" in {
      val matchRule = GroupViolationMatcher( description = "ftw" )
      sampleGroup should matchRule
    }

    "correctly match a rule violation based on children" in {
      val matchChildRule = RuleViolationMatcher( value = "value", constraint = "constraint", description = "description" )
      val matchRule = GroupViolationMatcher( violations = Seq( matchChildRule ) )
      sampleGroup should matchRule
    }

    "fail to match a non-matching group violation due to a constraint on a group property" in {
      val matchRule = GroupViolationMatcher( value = "incorrect" )
      sampleGroup should not( matchRule )
    }

    "fail to match a non-matching group violation due to a nonmatching child rule" in {
      val nonmatchingChildRule = RuleViolationMatcher( value = "incorrect" )
      val matchRule = GroupViolationMatcher( violations = Seq( nonmatchingChildRule ) )
      sampleGroup should not( matchRule )
    }
  }

  "Matcher construction DSL" should {

    "generate a correct rule violation for a Tuple2[String, String]" in {
      val rv: RuleViolationMatcher = "description" -> "constraint"
      rv.description shouldEqual "description"
      rv.constraint shouldEqual "constraint"
      rv.value should beNull
    }

    "generate a correct group violation via group()" in {
      val gv = group( "description", "constraint", "description" -> "constraint" )
      gv.description shouldEqual "description"
      gv.constraint shouldEqual "constraint"
      gv.value should beNull
      gv.violations should have size 1
      gv.violations.head should beLike {
        case rv: RuleViolationMatcher =>
          ( rv.description shouldEqual "description" ) and
          ( rv.constraint shouldEqual "constraint" ) and
          ( rv.value should beNull )
      }
    }
  }

  "failWith matcher" should {

    val result: Result = Failure( Seq( RuleViolation( "value", "constraint", "description" ) ) )

    "succeed if a validation rule matches successfully" in {
      result should failWith( "description" -> "constraint" )
    }

    "fail if a validation rule does not match" in {
      result should not( failWith( "invalid" -> "invalid" ) )
    }
  }

  "succeed matcher" should {

    "succeed if matching a Success" in {
      Success should succeed
    }

    "fail if matching a Failure" in {
      Failure( Seq.empty ) should not( succeed )
    }
  }

  "fail matcher" should {

    "fail if matching a Success" in {
      Success should not( fail )
    }

    "succeed if matching a Failure" in {
      Failure( Seq.empty ) should fail
    }
  }
}