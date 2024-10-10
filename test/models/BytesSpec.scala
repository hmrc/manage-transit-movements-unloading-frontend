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

import base.SpecBase

class BytesSpec extends SpecBase {

  "getSize" - {
    "when size is 1 byte" - {
      "must return 1 B" in {
        val result = Bytes.apply(1)
        result.value.mustBe("1 B")
      }
    }

    "when size is 1234 bytes" - {
      "must return 1.21 KB" in {
        val result = Bytes.apply(1234)
        result.value.mustBe("1.21 KB")
      }
    }

    "when size is 1234567 bytes" - {
      "must return 1.18 MB" in {
        val result = Bytes.apply(1234567)
        result.value.mustBe("1.18 MB")
      }
    }

    "when size is 1234567890 bytes" - {
      "must return 1.15 MB" in {
        val result = Bytes.apply(1234567890)
        result.value.mustBe("1.15 GB")
      }
    }
  }
}
