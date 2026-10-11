sealed class Row {
    data class Header(val text: String) : Row()
    data class Section(val text: String) : Row()
    data class App(val app: AppInfo) : Row()
}
