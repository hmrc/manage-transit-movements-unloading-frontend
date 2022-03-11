/*
 * Copyright 2022 HM Revenue & Customs
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

package config

import com.google.inject.AbstractModule
import connectors.{UnloadingConnector, UnloadingConnectorImpl}
import controllers.actions._
import services._

import java.time.{Clock, ZoneOffset}

class Module extends AbstractModule {

  override def configure(): Unit = {

    bind(classOf[DataRetrievalActionProvider]).to(classOf[DataRetrievalActionProviderImpl]).asEagerSingleton()

    bind(classOf[DataRequiredAction]).to(classOf[DataRequiredActionImpl]).asEagerSingleton()

    // For session based storage instead of cred based, change to SessionIdentifierAction
    bind(classOf[IdentifierAction]).to(classOf[AuthenticatedIdentifierAction]).asEagerSingleton()

    bind(classOf[UnloadingConnector]).to(classOf[UnloadingConnectorImpl]).asEagerSingleton()

    bind(classOf[UnloadingPermissionService]).to(classOf[UnloadingPermissionServiceImpl]).asEagerSingleton()

    bind(classOf[RemarksService]).to(classOf[RemarksServiceImpl]).asEagerSingleton()

    bind(classOf[ResultOfControlService]).to(classOf[ResultOfControlServiceImpl]).asEagerSingleton()

    bind(classOf[ReferenceDataService]).to(classOf[ReferenceDataServiceImpl]).asEagerSingleton()

    bind(classOf[DateTimeService]).to(classOf[DateTimeServiceImpl]).asEagerSingleton()

    bind(classOf[MetaService]).to(classOf[MetaServiceImpl]).asEagerSingleton()

    bind(classOf[UnloadingRemarksRequestService]).to(classOf[UnloadingRemarksRequestServiceImpl]).asEagerSingleton()

    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone.withZone(ZoneOffset.UTC))

  }

}
