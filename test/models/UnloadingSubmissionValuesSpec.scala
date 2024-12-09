/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models

import generated.{Number0, Number1}
import models.UnloadingSubmissionValues._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class UnloadingSubmissionValuesSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks {

  "UnloadingSubmissionValues" - {

    "must have correct value for type" - {
      "Conform must have value Number1" in {
        Conform.value mustEqual Number1
      }
      "NotConform must have value Number1" in {
        NotConform.value mustEqual Number0
      }
      "FullyUnloaded must have value Number1" in {
        FullyUnloaded.value mustEqual Number1
      }
      "PresentAndNotDamaged must have value Number1" in {
        PresentAndNotDamaged.value mustEqual Number1
      }
    }

    "unloadingSubmissionValueToFlag" - {
      "must implicitly return the value of the UnloadingSubmissionValue" - {
        "for Conform" in {
          unloadingSubmissionValueToFlag(Conform) mustEqual Number1
        }
        "for NotConform" in {
          unloadingSubmissionValueToFlag(NotConform) mustEqual Number0
        }
        "for FullyUnloaded" in {
          unloadingSubmissionValueToFlag(FullyUnloaded) mustEqual Number1
        }
        "for PresentAndNotDamaged" in {
          unloadingSubmissionValueToFlag(PresentAndNotDamaged) mustEqual Number1
        }
      }
    }
  }
}
