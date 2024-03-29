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

package models.requests

import generated.CC043CType
import models.EoriNumber
import play.api.mvc.{Request, WrappedRequest}

case class UnloadingPermissionRequest[A](
  request: Request[A],
  eoriNumber: EoriNumber,
  unloadingPermission: CC043CType
) extends WrappedRequest[A](request)
