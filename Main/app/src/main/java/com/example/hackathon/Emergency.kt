data class Emergency(
    val address: String = "",
    val fullName: String = "",
    val idNumber: String = "",
    val phoneNumber: String = "",
    val details: String = "",
    val status: String = "",
    val gender: String = "",
    val birthDate: String = "",
    val emergencyDetails: String = "",
    val location: GeoLocation = GeoLocation() // Default empty location
) {
    val reportId: String = ""

    data class GeoLocation(
        val latitude: Double = 0.0,
        val longitude: Double = 0.0
    )
}
