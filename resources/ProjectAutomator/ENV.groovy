environments {
	DEV {
		IntegrationServers {
			is_node1 {
				host = "localhost"
				port = "8094"
				username = "Administrator"
				pwd = "manage"
				useSSL = "false"
				version = "9.12"
				installDeployerResource  = "true"
				test = "true"
			}
		}
	}
	TEST {
                IntegrationServers {
                        is_node1 {
                                host = "localhost"
                                port = "8094"
                                username = "Administrator"
                                pwd = "manage"
                                useSSL = "false"
                                version = "9.12"
                                installDeployerResource  = "true"
                                test = "true"
                        }
                }
        }

	PRE_PROD {
		IntegrationServers {
			is_node1 {
				host = "localhost"
				port = "8080"
				username = "Administrator"
				pwd = "manage"
				useSSL = "false"
				version = "9.10"
				installDeployerResource  = "true"
				test = "true"
			}
			is_node2 {
				host = "localhost"
				port = "8080"
				username = "Administrator"
				pwd = "manage"
				useSSL = "false"
				version = "9.10"
				installDeployerResource  = "true"
				test = "true"
			}
		}
		ProcessModels {
			bpm_node1 {
				host = "localhost"
				port = "5555"
				username = "Administrator"
				pwd = "manage"
				useSSL = "false"
				version = "9.10"
				test = "true"
			}
			bpm_node2 {
				host = "localhost"
				port = "5555"
				username = "Administrator"
				pwd = "manage"
				useSSL = "false"
				version = "9.10"
				test = "true"
			}
		}
		MWS {
			mws_node1 {
				host = "localhost"
				port = "5555"
				username = "Administrator"
				pwd = "manage"
				useSSL = "false"
				version = "9.10"
				test = "true"
				excludeCoreTaskEngineDependencies = "true"
				cacheTimeOut = "0"
				includeSecurityDependencies = "true"
				rootFolderAliases = "folder.public"
				maximumFolderObjectCount = "8000"
				enableAddtionalLogging = "true"
				maxFolderDepth = "25"
			}
		}
	}
	PROD {
		IntegrationServers {
			is_node1 {
				host = "localhost"
				port = "8080"
				username = "Administrator"
				pwd = "manage"
				useSSL = "false"
				version = "9.10"
				installDeployerResource  = "true"
				test = "true"
				executeACL = "Administrator"
			}
			is_node2 {
				host = "localhost"
				port = "8080"
				username = "Administrator"
				pwd = "manage"
				useSSL = "false"
				version = "9.10"
				installDeployerResource  = "true"
				test = "true"
				executeACL = "Administrator"
			}
		}
	}
}
