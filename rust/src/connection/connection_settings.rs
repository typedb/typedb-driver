use std::path::Path;
use std::fs;
use tonic::transport::{Certificate, ClientTlsConfig};

#[derive(Debug)]
pub struct ConnectionSettings {
    is_tls_enabled: bool,
    tls_config: Option<ClientTlsConfig>,
}

impl ConnectionSettings {
    /// Creates a credential with username and password. Specifies the connection must use TLS
    ///
    /// # Arguments
    ///
    /// * `tls_root_ca` -- Path to the CA certificate to use for authenticating server certificates.
    ///
    /// # Examples
    ///
    /// ```rust
    /// ConnectionSetting::with_tls(Some(&path_to_ca));
    ///```
    pub fn with_tls(tls_root_ca: Option<&Path>) -> crate::Result<Self> {
        let tls_config = Some(if let Some(tls_root_ca) = tls_root_ca {
            ClientTlsConfig::new().ca_certificate(Certificate::from_pem(fs::read_to_string(tls_root_ca)?))
        } else {
            ClientTlsConfig::new()
        });

        Ok(Self { is_tls_enabled: true, tls_config })
    }

    /// Retrieves whether TLS is enabled for the connection.
    pub fn is_tls_enabled(&self) -> bool {
        self.is_tls_enabled
    }

    pub fn tls_config(&self) -> &Option<ClientTlsConfig> {
        &self.tls_config
    }
}