/*
 * Copyright 2023 HM Revenue & Customs
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

import cats.data.NonEmptyList
import com.lucidchart.open.xtract._

package object xml {

  implicit object NonEmptyListOps {

    implicit def nonEmptyListReader[A: XmlReader]: XmlReader[NonEmptyList[A]] =
      XmlReader
        .of[List[A]]
        .collect {
          case head :: tail => NonEmptyList(head, tail)
        }

    implicit def list[A](implicit reader: XmlReader[A]): XmlReader[List[A]] = XmlReader {
      xml =>
        ParseResult.combine(xml.map(reader.read))
    }
  }
}
