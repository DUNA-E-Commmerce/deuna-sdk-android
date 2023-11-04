package com.deuna.maven.element.domain

enum class ElementEvent(val value: String) {
    vaultClosed("vaultClosed"),
    vaultProcessing("vaultProcessing"),
    vaultSaveClick("vaultSaveClick"),
    vaultStarted("vaultStarted"),
    vaultFailed("vaultFailed"),
    cardSuccessfullyCreated("cardSuccessfullyCreated"),
    vaultSavingError("vaultSavingError"),
    vaultSavingSuccess("vaultSavingSuccess"),
    vaulClickRedirect3DS("vaulClickRedirect3DS"),
    cardCreationError("cardCreationError")
}