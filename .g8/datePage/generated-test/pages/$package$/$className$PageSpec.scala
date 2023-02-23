package pages.$package$

import pages.behaviours.PageBehaviours

import java.time.LocalDate

class $className$PageSpec extends PageBehaviours {

  "$className$Page" - {

    beRetrievable[LocalDate]($className$Page)

    beSettable[LocalDate]($className$Page)

    beRemovable[LocalDate]($className$Page)
  }
}
