import {
  InteractionRequiredAuthError,
  PublicClientApplication
} from "@azure/msal-browser";

import "./style.css";

const environment = {
  tenantId: import.meta.env.VITE_ENTRA_TENANT_ID,
  spaClientId: import.meta.env.VITE_ENTRA_SPA_CLIENT_ID,
  apiScope: import.meta.env.VITE_ENTRA_API_SCOPE,
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL
};

validateEnvironment(environment);

const apiBaseUrl = environment.apiBaseUrl.replace(/\/$/, "");

const msalConfig = {
  auth: {
    clientId: environment.spaClientId,
    authority:
      `https://login.microsoftonline.com/${environment.tenantId}`,
    redirectUri: window.location.origin,
    postLogoutRedirectUri: window.location.origin,
    navigateToLoginRequestUrl: false
  },
  cache: {
    cacheLocation: "sessionStorage"
  }
};

const tokenRequest = {
  scopes: [environment.apiScope]
};

const msalInstance = new PublicClientApplication(msalConfig);

const elements = {
  sessionStatus: document.querySelector("#session-status"),
  loginButton: document.querySelector("#login-button"),
  logoutButton: document.querySelector("#logout-button"),
  coursesButton: document.querySelector("#courses-button"),
  userPanel: document.querySelector("#user-panel"),
  userName: document.querySelector("#user-name"),
  userUsername: document.querySelector("#user-username"),
  tokenAudience: document.querySelector("#token-audience"),
  tokenScope: document.querySelector("#token-scope"),
  apiOutput: document.querySelector("#api-output"),
  technicalOutput: document.querySelector("#technical-output")
};

initializeApplication();

async function initializeApplication() {
  try {
    await msalInstance.initialize();
    await restoreSession();

    elements.loginButton.addEventListener("click", login);
    elements.logoutButton.addEventListener("click", logout);
    elements.coursesButton.addEventListener("click", loadCourses);
  } catch (error) {
    renderError(
      "No fue posible inicializar Microsoft Entra ID.",
      error
    );
  }
}

async function login() {
  setBusy(true);

  try {
    const response = await msalInstance.loginPopup({
      scopes: [environment.apiScope],
      prompt: "select_account"
    });

    msalInstance.setActiveAccount(response.account);

    const tokenResponse = await acquireAccessToken(
      response.account,
      true
    );

    renderAuthenticated(response.account, tokenResponse);

    elements.apiOutput.textContent =
      "Autenticación correcta. Ya puedes consultar la API.";
  } catch (error) {
    renderError("No fue posible iniciar sesión.", error);
  } finally {
    setBusy(false);
  }
}

async function logout() {
  const account = getCurrentAccount();

  if (!account) {
    renderLoggedOut();
    return;
  }

  setBusy(true);

  try {
    await msalInstance.logoutPopup({
      account,
      postLogoutRedirectUri: window.location.origin,
      mainWindowRedirectUri: window.location.origin
    });

    renderLoggedOut();
  } catch (error) {
    renderError("No fue posible cerrar la sesión.", error);
  } finally {
    setBusy(false);
  }
}

async function loadCourses() {
  const account = getCurrentAccount();

  if (!account) {
    renderLoggedOut();
    return;
  }

  setBusy(true);
  elements.apiOutput.textContent =
    "Consultando cursos mediante el API Gateway...";

  try {
    const tokenResponse = await acquireAccessToken(
      account,
      true
    );

    const response = await fetch(
      `${apiBaseUrl}/cursos`,
      {
        method: "GET",
        headers: {
          Authorization:
            `Bearer ${tokenResponse.accessToken}`,
          Accept: "application/json"
        }
      }
    );

    const responseText = await response.text();
    const responseBody = parseResponseBody(responseText);

    if (!response.ok) {
      throw new Error(
        `HTTP ${response.status}: ${
          JSON.stringify(responseBody)
        }`
      );
    }

    elements.apiOutput.textContent =
      JSON.stringify(responseBody, null, 2);

    renderAuthenticated(account, tokenResponse);
  } catch (error) {
    renderError(
      "La autenticación funciona, pero la consulta a la API falló.",
      error
    );
  } finally {
    setBusy(false);
  }
}

async function restoreSession() {
  const accounts = msalInstance.getAllAccounts();

  if (accounts.length === 0) {
    renderLoggedOut();
    return;
  }

  const account = accounts[0];
  msalInstance.setActiveAccount(account);

  try {
    const tokenResponse = await acquireAccessToken(
      account,
      false
    );

    renderAuthenticated(account, tokenResponse);
  } catch (error) {
    renderAuthenticatedWithoutToken(account);

    elements.technicalOutput.textContent =
      "La cuenta fue recuperada, pero se requiere una " +
      "interacción para obtener un token nuevo.";
  }
}

async function acquireAccessToken(
  account,
  allowInteractive
) {
  try {
    return await msalInstance.acquireTokenSilent({
      ...tokenRequest,
      account
    });
  } catch (error) {
    if (
      allowInteractive &&
      error instanceof InteractionRequiredAuthError
    ) {
      return msalInstance.acquireTokenPopup({
        ...tokenRequest,
        account
      });
    }

    throw error;
  }
}

function renderAuthenticated(account, tokenResponse) {
  const claims = decodeJwtPayload(tokenResponse.accessToken);

  elements.sessionStatus.textContent = "Sesión iniciada";
  elements.sessionStatus.className =
    "status status-online";

  elements.loginButton.hidden = true;
  elements.logoutButton.hidden = false;
  elements.userPanel.hidden = false;

  elements.userName.textContent =
    account.name ?? claims.name ?? "Usuario autenticado";

  elements.userUsername.textContent =
    account.username ??
    claims.preferred_username ??
    "No disponible";

  elements.tokenAudience.textContent =
    formatClaim(claims.aud);

  elements.tokenScope.textContent =
    formatClaim(claims.scp);

  elements.technicalOutput.textContent =
    JSON.stringify(
      {
        tenantId: account.tenantId,
        audience: claims.aud,
        scope: claims.scp,
        issuer: claims.iss,
        expiresAt: formatExpiration(claims.exp)
      },
      null,
      2
    );
}

function renderAuthenticatedWithoutToken(account) {
  elements.sessionStatus.textContent =
    "Cuenta detectada";

  elements.sessionStatus.className =
    "status status-warning";

  elements.loginButton.hidden = false;
  elements.logoutButton.hidden = false;
  elements.userPanel.hidden = false;

  elements.userName.textContent =
    account.name ?? "Usuario autenticado";

  elements.userUsername.textContent =
    account.username ?? "No disponible";

  elements.tokenAudience.textContent = "Pendiente";
  elements.tokenScope.textContent = "Pendiente";
}

function renderLoggedOut() {
  msalInstance.setActiveAccount(null);

  elements.sessionStatus.textContent =
    "Sesión no iniciada";

  elements.sessionStatus.className =
    "status status-offline";

  elements.loginButton.hidden = false;
  elements.logoutButton.hidden = true;
  elements.userPanel.hidden = true;

  elements.userName.textContent = "—";
  elements.userUsername.textContent = "—";
  elements.tokenAudience.textContent = "—";
  elements.tokenScope.textContent = "—";

  elements.apiOutput.textContent =
    "Inicia sesión y consulta el endpoint protegido.";

  elements.technicalOutput.textContent =
    "Microsoft Entra ID pendiente de autenticación.";
}

function renderError(message, error) {
  const details = getErrorDetails(error);

  elements.technicalOutput.textContent =
    `${message}\n\n${details}`;

  console.error(message, error);
}

function setBusy(isBusy) {
  elements.loginButton.disabled = isBusy;
  elements.logoutButton.disabled = isBusy;
  elements.coursesButton.disabled = isBusy;
}

function getCurrentAccount() {
  return (
    msalInstance.getActiveAccount() ??
    msalInstance.getAllAccounts()[0] ??
    null
  );
}

function validateEnvironment(config) {
  const missing = Object.entries(config)
    .filter(([, value]) => !value)
    .map(([key]) => key);

  if (missing.length > 0) {
    throw new Error(
      `Faltan variables de entorno: ${missing.join(", ")}`
    );
  }
}

function decodeJwtPayload(token) {
  const parts = token.split(".");

  if (parts.length !== 3) {
    throw new Error("El access token no tiene formato JWT.");
  }

  const normalized = parts[1]
    .replace(/-/g, "+")
    .replace(/_/g, "/");

  const padded = normalized.padEnd(
    normalized.length +
      ((4 - (normalized.length % 4)) % 4),
    "="
  );

  const binary = window.atob(padded);

  const bytes = Uint8Array.from(
    binary,
    character => character.charCodeAt(0)
  );

  return JSON.parse(
    new TextDecoder().decode(bytes)
  );
}

function parseResponseBody(responseText) {
  if (!responseText) {
    return null;
  }

  try {
    return JSON.parse(responseText);
  } catch {
    return responseText;
  }
}

function formatClaim(value) {
  if (Array.isArray(value)) {
    return value.join(", ");
  }

  return value ?? "No disponible";
}

function formatExpiration(expiration) {
  if (!expiration) {
    return "No disponible";
  }

  return new Date(expiration * 1000)
    .toLocaleString("es-CL");
}

function getErrorDetails(error) {
  if (error instanceof Error) {
    return [
      error.name,
      error.message
    ]
      .filter(Boolean)
      .join(": ");
  }

  return String(error);
}