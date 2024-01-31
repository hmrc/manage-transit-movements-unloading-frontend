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

package generators

import cats.data.NonEmptyList
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalacheck.{Arbitrary, Gen, Shrink}
import wolfendale.scalacheck.regexp.RegexpGen

import java.time._
import scala.util.matching.Regex

trait Generators
    extends UserAnswersGenerator
    with PageGenerators
    with ModelGenerators
    with UserAnswersEntryGenerators
    with ViewModelGenerators
    with MessagesModelGenerators {

  implicit val dontShrink: Shrink[String] = Shrink.shrinkAny
  private val maxListLength               = 10

  def genIntersperseString(gen: Gen[String], value: String, frequencyV: Int = 1, frequencyN: Int = 10): Gen[String] = {

    val genValue: Gen[Option[String]] = Gen.frequency(frequencyN -> None, frequencyV -> Gen.const(Some(value)))

    for {
      seq1 <- gen
      seq2 <- Gen.listOfN(seq1.length, genValue)
    } yield seq1.toSeq.zip(seq2).foldRight("") {
      case ((n, Some(v)), m) =>
        m + n + v
      case ((n, _), m) =>
        m + n
    }
  }

  def positiveInts: Gen[Int] = Gen.choose(0, Int.MaxValue)

  def positiveBigInts: Gen[BigInt] = Gen.choose(0, 1000)

  def positiveBigDecimals: Gen[BigDecimal] = Gen.choose(0, 1000)

  def intsInRangeWithCommas(min: Int, max: Int): Gen[String] = {
    val numberGen = choose[Int](min, max)
    genIntersperseString(numberGen.toString, ",")
  }

  def stringsWithLengthNotEqual(length: Int, charGen: Gen[Char] = Gen.alphaNumChar): Gen[String] =
    for {
      len   <- Gen.posNum[Int].filter(_ != length)
      chars <- listOfN(len, charGen)
    } yield chars.mkString

  def intsLargerThanMaxValue: Gen[BigInt] =
    arbitrary[BigInt] suchThat (
      x => x > Int.MaxValue
    )

  def intsSmallerThanMinValue: Gen[BigInt] =
    arbitrary[BigInt] suchThat (
      x => x < Int.MinValue
    )

  def nonNumerics: Gen[String] =
    Gen.nonEmptyListOf[Char](Gen.alphaChar).map(_.mkString)

  def decimals: Gen[String] =
    arbitrary[BigDecimal]
      .suchThat(_.abs < Int.MaxValue)
      .suchThat(!_.isValidInt)
      .map("%f".format(_))

  def intsBelowValue(value: Int): Gen[Int] =
    arbitrary[Int] suchThat (_ < value)

  def intsAboveValue(value: Int): Gen[Int] =
    arbitrary[Int] suchThat (_ > value)

  def intsOutsideRange(min: Int, max: Int): Gen[Int] =
    arbitrary[Int] suchThat (
      x => x < min || x > max
    )

  def nonBooleans: Gen[String] =
    nonEmptyString
      .suchThat(_ != "true")
      .suchThat(_ != "false")

  def nonEmptyString: Gen[String] =
    Gen.nonEmptyListOf[Char](Gen.alphaNumChar).map(_.mkString)

  def stringsThatMatchRegex(regex: Regex): Gen[String] =
    RegexpGen.from(regex.regex).suchThat(_.nonEmpty)

  def stringsWithMaxLength(maxLength: Int): Gen[String] =
    for {
      length <- choose(1, maxLength)
      chars  <- listOfN(length, alphaNumChar)
    } yield chars.mkString

  def stringsLongerThan(minLength: Int, charGen: Gen[Char] = Gen.alphaNumChar): Gen[String] =
    for {
      maxLength <- (minLength * 2).max(100)
      length    <- Gen.chooseNum(minLength + 1, maxLength)
      chars     <- listOfN(length, charGen)
    } yield chars.mkString

  def stringsExceptSpecificValues(excluded: Seq[String]): Gen[String] =
    nonEmptyString suchThat (!excluded.contains(_))

  def oneOf[T](xs: Seq[Gen[T]]): Gen[T] =
    if (xs.isEmpty) {
      throw new IllegalArgumentException("oneOf called on empty collection")
    } else {
      val vector = xs.toVector
      choose(0, vector.size - 1).flatMap(vector(_))
    }

  def datesBetween(min: LocalDate, max: LocalDate): Gen[LocalDate] = {

    def toMillis(date: LocalDate): Long =
      date.atStartOfDay.atZone(ZoneOffset.UTC).toInstant.toEpochMilli

    Gen.choose(toMillis(min), toMillis(max)).map {
      millis =>
        Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDate
    }
  }

  def listWithMaxLength[A](maxLength: Int = maxListLength)(implicit a: Arbitrary[A]): Gen[List[A]] =
    for {
      length <- choose(1, maxLength)
      seq    <- listOfN(length, arbitrary[A])
    } yield seq

  def distinctListWithMaxLength[A, B](maxLength: Int = maxListLength)(f: A => B)(implicit a: Arbitrary[A]): Gen[List[A]] =
    for {
      values <- listWithMaxLength[A](maxLength)
    } yield values.distinctBy(f)

  def listWithMaxSize[T](maxSize: Int, gen: Gen[T]): Gen[Seq[T]] =
    for {
      size  <- Gen.choose(0, maxSize)
      items <- Gen.listOfN(size, gen)
    } yield items

  def listOfSize[T](size: Int, gen: Gen[T]): Gen[Seq[T]] =
    for {
      items <- Gen.listOfN(size, gen)
    } yield items

  def nonEmptyListWithMaxSize[T](maxSize: Int, gen: Gen[T]): Gen[NonEmptyList[T]] =
    for {

      head     <- gen
      tailSize <- Gen.choose(1, maxSize - 1)
      tail     <- Gen.listOfN(tailSize, gen)
    } yield NonEmptyList(head, tail)

  def dateTimesBetween(min: LocalDateTime, max: LocalDateTime): Gen[LocalDateTime] = {

    def toMillis(date: LocalDateTime): Long =
      date.atZone(ZoneOffset.UTC).toInstant.toEpochMilli

    Gen.choose(toMillis(min), toMillis(max)).map {
      millis =>
        Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDateTime
    }
  }

  implicit lazy val arbitraryLocalDate: Arbitrary[LocalDate] = Arbitrary {
    datesBetween(LocalDate.of(1900, 1, 1), LocalDate.of(2100, 1, 1))
  }

  implicit lazy val arbitraryLocalTime: Arbitrary[LocalTime] = Arbitrary {
    dateTimesBetween(
      LocalDateTime.of(1900, 1, 1, 0, 0, 0),
      LocalDateTime.of(2100, 1, 1, 0, 0, 0)
    ).map(_.toLocalTime)
  }

  implicit lazy val arbitraryLocalDateTime: Arbitrary[LocalDateTime] = Arbitrary {
    dateTimesBetween(
      LocalDateTime.of(1900, 1, 1, 0, 0, 0),
      LocalDateTime.of(2100, 1, 1, 0, 0, 0)
    )
  }

  val localDateGen: Gen[LocalDate] = datesBetween(LocalDate.of(1900, 1, 1), LocalDate.now)

}
