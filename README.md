## Универсальный плагин для добавления ссылок в инфраструктуру компании.

### Поддерживает:

- Добавление ссылок в контекстное меню по определенным файлам
- Добавление ссылок на полях в xml файлы
- Добавление ссылок на полях в yaml конфигах
- Добавление ссылок по вызову метода определенного класса (Java / Kotlin)
- Добавление ссылки на полях на аннотации
- Добавление ссылки реализации интерфейса

Плагин настраивается с помощью конфигурации src/main/resources/config/infra-config.json

### Возможные переменные для замены в ссылках:

- fileNameWithPath
- yamlValue
- appName
- currentBranchName
- parameter1 / parameter2 / parameter3 и тд
- andQueryStringParameters
- annotationKey (напр для @MyAnnotation(name = "example") надо добавлять через annotationName)
- projectDir (Можно указывать ссылку на файл в проекте idea://open?file={projectDir}/.chart/config-preprod.yaml)

Чтобы сделать плагин под себя - копируем src/main/resources/config/infra-config-sample.json со своими настройками в src/main/resources/config/infra-config.json
Тестируем в sandbox на своем окружении через gradle таску :runIde
Собираем плагин через gradle таску :buildPlugin и распространяем у себя в компании

Подробнее как плагин создавался можно почитать в статье https://habr.com/ru/companies/alfa/articles/900214/