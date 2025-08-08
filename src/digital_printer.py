import os

# Define la ruta al archivo de número de control de forma relativa.
CONFIG_DIR = os.path.join(os.path.dirname(__file__), '..', 'config')
CONTROL_NUMBER_FILE = os.path.join(CONFIG_DIR, 'control_number.txt')

def get_next_control_number() -> str:
    """
    Lee el último número de control del archivo, lo incrementa,
    lo guarda de nuevo y lo devuelve como una cadena formateada.

    Maneja la concurrencia de manera simple (lectura-escritura),
    pero para un sistema de alta concurrencia se necesitaría un bloqueo de archivo.

    Returns:
        str: El siguiente número de control, formateado como una cadena de 8 dígitos.
    """
    if not os.path.exists(CONFIG_DIR):
        os.makedirs(CONFIG_DIR)

    try:
        with open(CONTROL_NUMBER_FILE, 'r') as f:
            last_number = int(f.read().strip())
    except (FileNotFoundError, ValueError):
        # Si el archivo no existe o está corrupto/vacío, empezamos desde 0.
        last_number = 0

    next_number = last_number + 1

    with open(CONTROL_NUMBER_FILE, 'w') as f:
        f.write(str(next_number))

    # Formatea el número a una cadena de 8 dígitos con ceros iniciales.
    return f"{next_number:08d}"
