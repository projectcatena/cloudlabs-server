package com.cloudlabs.server.compute;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface ComputeService {
    boolean deleteInstance(String project, String zone, String instanceName) throws IOException, InterruptedException, ExecutionException, TimeoutException;
}