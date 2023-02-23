package pages.$package$

import pages.behaviours.PageBehaviours
import models.DynamicAddress

class $className$PageSpec extends PageBehaviours {

  "$className$Page" - {

    beRetrievable[DynamicAddress]($className$Page)

    beSettable[DynamicAddress]($className$Page)

    beRemovable[DynamicAddress]($className$Page)
  }
}
