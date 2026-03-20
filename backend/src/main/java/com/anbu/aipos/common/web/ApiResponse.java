package com.anbu.aipos.common.web;

public record ApiResponse<T>(String message, T data) {
}
