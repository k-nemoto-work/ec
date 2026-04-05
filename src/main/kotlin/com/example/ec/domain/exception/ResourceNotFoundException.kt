package com.example.ec.domain.exception

class ResourceNotFoundException(resourceName: String, id: String) :
    RuntimeException("${resourceName}が見つかりません: $id")
