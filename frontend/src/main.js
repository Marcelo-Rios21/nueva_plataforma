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
  operationsCard: document.querySelector("#operations-card"),
  inscriptionForm: document.querySelector("#inscription-form"),
  createInscriptionButton:
    document.querySelector("#create-inscription-button"),
  studentIdInput: document.querySelector("#student-id"),
  courseIdsInput: document.querySelector("#course-ids"),
  paymentMethodInput: document.querySelector("#payment-method"),
  consumeMqButton: document.querySelector("#consume-mq-button"),
  listMqButton: document.querySelector("#list-mq-button"),
  s3InscriptionId: document.querySelector("#s3-inscription-id"),
  uploadS3Button: document.querySelector("#upload-s3-button"),
  downloadS3Button: document.querySelector("#download-s3-button"),
  workflowOutput: document.querySelector("#workflow-output"),
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

    elements.inscriptionForm.addEventListener(
      "submit",
      createInscription
    );

    elements.consumeMqButton.addEventListener(
      "click",
      consumeRabbitMq
    );

    elements.listMqButton.addEventListener(
      "click",
      listRabbitMq
    );

    elements.uploadS3Button.addEventListener(
      "click",
      uploadSummaryS3
    );

    elements.downloadS3Button.addEventListener(
      "click",
      downloadSummaryS3
    );
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

async function createInscription(event) {
  event.preventDefault();
  setBusy(true);

  try {
    const estudianteId = parsePositiveInteger(
      elements.studentIdInput.value,
      "ID del estudiante"
    );

    const cursoIds = parseCourseIds(
      elements.courseIdsInput.value
    );

    const metodoPago =
      elements.paymentMethodInput.value.trim();

    if (!metodoPago) {
      throw new Error(
        "El método de pago es obligatorio."
      );
    }

    const response = await fetchJsonWithToken(
      "/bff/inscripciones",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          estudianteId,
          cursoIds,
          metodoPago
        })
      }
    );

    const inscripcionId =
      response?.inscripcion?.inscripcionId;

    if (inscripcionId) {
      elements.s3InscriptionId.value =
        String(inscripcionId);
    }

    renderWorkflowResult(
      "Inscripción creada y resumen publicado en RabbitMQ",
      response
    );
  } catch (error) {
    renderWorkflowError(
      "No fue posible crear la inscripción.",
      error
    );
  } finally {
    setBusy(false);
  }
}

async function consumeRabbitMq() {
  setBusy(true);

  try {
    const response = await fetchJsonWithToken(
      "/bff/mq/resumenes/consumir",
      {
        method: "POST"
      }
    );

    renderWorkflowResult(
      "Mensaje consumido desde RabbitMQ",
      response
    );
  } catch (error) {
    renderWorkflowError(
      "No fue posible consumir el mensaje.",
      error
    );
  } finally {
    setBusy(false);
  }
}

async function listRabbitMq() {
  setBusy(true);

  try {
    const response = await fetchJsonWithToken(
      "/bff/mq/resumenes"
    );

    renderWorkflowResult(
      "Resúmenes guardados después del consumo",
      response
    );
  } catch (error) {
    renderWorkflowError(
      "No fue posible listar los mensajes guardados.",
      error
    );
  } finally {
    setBusy(false);
  }
}

async function uploadSummaryS3() {
  setBusy(true);

  try {
    const inscripcionId = getS3InscriptionId();

    const response = await fetchJsonWithToken(
      `/bff/inscripciones/${inscripcionId}/resumen/s3`,
      {
        method: "POST"
      }
    );

    renderWorkflowResult(
      "Resumen subido correctamente a AWS S3",
      response
    );
  } catch (error) {
    renderWorkflowError(
      "No fue posible subir el resumen a S3.",
      error
    );
  } finally {
    setBusy(false);
  }
}

async function downloadSummaryS3() {
  setBusy(true);

  try {
    const inscripcionId = getS3InscriptionId();

    const response = await fetchWithToken(
      `/bff/inscripciones/${inscripcionId}/resumen/s3/download`,
      {
        method: "GET",
        headers: {
          Accept: "text/plain"
        }
      }
    );

    if (!response.ok) {
      const responseText = await response.text();
      const responseBody = parseResponseBody(responseText);

      throw new Error(
        `HTTP ${response.status}: ${
          JSON.stringify(responseBody)
        }`
      );
    }

    const blob = await response.blob();

    const filename = getDownloadFilename(
      response.headers.get("Content-Disposition"),
      inscripcionId
    );

    const downloadUrl = URL.createObjectURL(blob);
    const link = document.createElement("a");

    link.href = downloadUrl;
    link.download = filename;

    document.body.appendChild(link);
    link.click();
    link.remove();

    URL.revokeObjectURL(downloadUrl);

    renderWorkflowResult(
      "Resumen descargado desde AWS S3",
      {
        inscripcionId,
        archivo: filename,
        bytes: blob.size
      }
    );
  } catch (error) {
    renderWorkflowError(
      "No fue posible descargar el resumen desde S3.",
      error
    );
  } finally {
    setBusy(false);
  }
}

async function fetchJsonWithToken(
  path,
  options = {}
) {
  const response = await fetchWithToken(path, options);
  const responseText = await response.text();
  const responseBody = parseResponseBody(responseText);

  if (!response.ok) {
    throw new Error(
      `HTTP ${response.status}: ${
        JSON.stringify(responseBody)
      }`
    );
  }

  return responseBody;
}

async function fetchWithToken(
  path,
  options = {}
) {
  const account = getCurrentAccount();

  if (!account) {
    throw new Error(
      "Debes iniciar sesión antes de usar esta operación."
    );
  }

  const tokenResponse = await acquireAccessToken(
    account,
    true
  );

  const headers = new Headers(
    options.headers ?? {}
  );

  headers.set(
    "Authorization",
    `Bearer ${tokenResponse.accessToken}`
  );

  if (!headers.has("Accept")) {
    headers.set("Accept", "application/json");
  }

  return fetch(
    `${apiBaseUrl}${path}`,
    {
      ...options,
      headers
    }
  );
}

function parseCourseIds(value) {
  const ids = value
    .split(",")
    .map(item => item.trim())
    .filter(Boolean)
    .map(item =>
      parsePositiveInteger(item, "ID de curso")
    );

  if (ids.length === 0) {
    throw new Error(
      "Debes ingresar al menos un ID de curso."
    );
  }

  return [...new Set(ids)];
}

function parsePositiveInteger(value, fieldName) {
  const parsed = Number(value);

  if (
    !Number.isInteger(parsed) ||
    parsed <= 0
  ) {
    throw new Error(
      `${fieldName} debe ser un número entero mayor que cero.`
    );
  }

  return parsed;
}

function getS3InscriptionId() {
  return parsePositiveInteger(
    elements.s3InscriptionId.value,
    "ID de inscripción"
  );
}

function getDownloadFilename(
  contentDisposition,
  inscripcionId
) {
  if (contentDisposition) {
    const match =
      /filename="?([^";]+)"?/i.exec(
        contentDisposition
      );

    if (match?.[1]) {
      return match[1];
    }
  }

  return `resumen-inscripcion-${inscripcionId}.txt`;
}

function renderWorkflowResult(title, data) {
  elements.workflowOutput.textContent =
    `${title}\n\n${JSON.stringify(data, null, 2)}`;
}

function renderWorkflowError(message, error) {
  elements.workflowOutput.textContent =
    `${message}\n\n${getErrorDetails(error)}`;

  console.error(message, error);
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
  elements.operationsCard.hidden = false;

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
  elements.operationsCard.hidden = true;

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
  elements.operationsCard.hidden = true;

  elements.workflowOutput.textContent =
    "Selecciona una operación para comenzar.";

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
  elements.createInscriptionButton.disabled = isBusy;
  elements.consumeMqButton.disabled = isBusy;
  elements.listMqButton.disabled = isBusy;
  elements.uploadS3Button.disabled = isBusy;
  elements.downloadS3Button.disabled = isBusy;
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