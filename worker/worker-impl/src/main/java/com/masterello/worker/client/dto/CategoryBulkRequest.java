package com.masterello.worker.client.dto;

import java.util.List;

public record CategoryBulkRequest(List<Integer> categoryCodes, boolean onlyServices, boolean active) {}

