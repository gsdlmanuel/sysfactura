import logging
import os

# Define la ruta al directorio de logs de forma relativa.
LOGS_DIR = os.path.join(os.path.dirname(__file__), '..', 'logs')

# Crea el directorio de logs si no existe.
if not os.path.exists(LOGS_DIR):
    os.makedirs(LOGS_DIR)

# Ruta completa para el archivo de log.
LOG_FILE_PATH = os.path.join(LOGS_DIR, 'activity.log')

def setup_logger():
    """
    Configura y devuelve un logger para la aplicación.
    El logger registrará todas las acciones importantes en un archivo.
    """
    # Obtiene una instancia del logger.
    logger = logging.getLogger('FacturacionLogger')
    logger.setLevel(logging.INFO)  # Establece el nivel mínimo de los mensajes a registrar.

    # Evita que los mensajes se propaguen al logger raíz.
    logger.propagate = False

    # Si el logger ya tiene manejadores, no los añade de nuevo.
    # Esto previene la duplicación de logs si el módulo se importa varias veces.
    if logger.hasHandlers():
        return logger

    # Crea un manejador de archivo (FileHandler).
    # Este manejador escribe los registros de log a un archivo.
    file_handler = logging.FileHandler(LOG_FILE_PATH, encoding='utf-8')

    # Crea un formateador y lo establece para el manejador.
    # Formato: MARCA_DE_TIEMPO - NIVEL - MENSAJE
    formatter = logging.Formatter('%(asctime)s - %(levelname)s - %(message)s', datefmt='%Y-%m-%d %H:%M:%S')
    file_handler.setFormatter(formatter)

    # Añade el manejador al logger.
    logger.addHandler(file_handler)

    return logger

# Crea una instancia del logger para ser importada por otros módulos.
log = setup_logger()
