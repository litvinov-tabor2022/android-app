package cz.jenda.tabor2022.exception

abstract class TagExceptions(message: String) : Exception(message)

class InvalidDataOnTag(message: String) : TagExceptions(message)

class TagCannotBeRead(message: String) : TagExceptions(message)

class TagCannotBeWritten(message: String) : TagExceptions(message)