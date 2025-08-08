import smtplib
import os
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
from email.mime.base import MIMEBase
from email import encoders
from src.logger import log

# --- Configuración del Servidor de Correo ---
# Es una buena práctica de seguridad usar variables de entorno para las credenciales.
# De esta forma, no se exponen en el código fuente.
SMTP_SERVER = "smtp.gmail.com"
SMTP_PORT = 587  # Puerto para TLS
SENDER_EMAIL = os.environ.get('GMAIL_SENDER_EMAIL')
SENDER_PASSWORD = os.environ.get('GMAIL_APP_PASSWORD') # Se recomienda una contraseña de aplicación de Google.

def send_invoice_email(recipient_email: str, xml_filepath: str):
    """
    Envía un correo electrónico con la factura XML como archivo adjunto a través de Gmail.

    Args:
        recipient_email: La dirección de correo del destinatario.
        xml_filepath: La ruta al archivo XML de la factura que se va a adjuntar.
    """
    if not SENDER_EMAIL or not SENDER_PASSWORD:
        log.warning("Credenciales de correo no configuradas. El correo no será enviado.")
        log.warning("Para habilitar el envío, configure las variables de entorno 'GMAIL_SENDER_EMAIL' y 'GMAIL_APP_PASSWORD'.")
        return

    log.info(f"Preparando el correo para ser enviado a: {recipient_email}")

    # Crear el mensaje de correo (MIME).
    msg = MIMEMultipart()
    msg['From'] = SENDER_EMAIL
    msg['To'] = recipient_email
    msg['Subject'] = f"Nueva Factura Electrónica: {os.path.basename(xml_filepath)}"

    # Cuerpo del correo.
    body = "Estimado cliente,\n\nAdjunto a este correo encontrará su factura en formato XML.\n\nGracias por su preferencia."
    msg.attach(MIMEText(body, 'plain', 'utf-8'))

    # Adjuntar el archivo XML.
    try:
        with open(xml_filepath, 'rb') as attachment:
            part = MIMEBase('application', 'octet-stream')
            part.set_payload(attachment.read())
        encoders.encode_base64(part)
        part.add_header(
            'Content-Disposition',
            f'attachment; filename="{os.path.basename(xml_filepath)}"',
        )
        msg.attach(part)
        log.info(f"Archivo adjuntado: {xml_filepath}")
    except FileNotFoundError:
        log.error(f"No se pudo adjuntar el archivo porque no se encontró en la ruta: {xml_filepath}")
        return

    # Enviar el correo a través del servidor SMTP de Gmail.
    try:
        server = smtplib.SMTP(SMTP_SERVER, SMTP_PORT)
        server.starttls()  # Iniciar conexión segura.
        server.login(SENDER_EMAIL, SENDER_PASSWORD)
        text = msg.as_string()
        server.sendmail(SENDER_EMAIL, recipient_email, text)
        server.quit()
        log.info(f"Correo enviado exitosamente a {recipient_email}")
    except smtplib.SMTPAuthenticationError:
        log.error("Error de autenticación con Gmail. Verifique el correo y la contraseña de aplicación.")
        log.error("Asegúrese de estar usando una 'Contraseña de Aplicación' de Google si tiene 2FA activado.")
    except Exception as e:
        log.error(f"Ocurrió un error inesperado al enviar el correo: {e}")
