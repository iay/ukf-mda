# Release Notes for `ukf-mda`

## Version 0.7.3 ##

* Implemented a `RegistrationAuthority` item metadata object, wrapping a `String` representing the `registrationAuthority` attribute of the `mdrpi:RegistrationInfo` element from the MDRPI specification.
* Implemented a `RegistrationAuthorityPopulationStage` stage to populate an entity's `RegistrationAuthority` item metadata.
* Extended the `UKItemIdentificationStrategy` to incorporate an item's `RegistrationAuthority` metadata.  Facilities are included to allow a set of registrar URIs to be ignored entirely (useful for "ours"), and to allow the mapping of registrar URIs to more readable display names.

## Version 0.7.2

* Implemented `IdPDisplayNameDuplicateDetectingStage`.  As well as resolving a problem with our previous XSLT implementation of this functionality, which was buggy in the presence of entities with the same display name in multiple languages, it also resolves:
	* Bugzilla 803: consider `mdui:DisplayName` when looking for duplicate IdP display names
	* Bugzilla 933: `check_aggregate` should have a case insensitive check for duplicate `OrganizationDisplayName`
	* (unlisted) ignore leading and trailing whitespace for IdP display name comparisons
