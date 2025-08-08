from typing import List
from src.models import EmpresaEmisora, Cliente, Producto, Factura
from src.digital_printer import get_next_control_number
from src.xml_generator import generate_xml
from src.logger import log

def create_new_invoice(emisor: EmpresaEmisora, cliente: Cliente, productos: List[Producto]) -> Factura:
    """
    Orquesta la creación de una nueva factura.

    Este es el flujo de trabajo principal para emitir una factura:
    1. Obtiene un nuevo número de control de la "imprenta digital".
    2. Crea un objeto Factura con todos los datos.
    3. Genera el archivo XML correspondiente a la factura.
    4. Registra el evento de creación para la trazabilidad.

    Args:
        emisor: El objeto de la empresa emisora.
        cliente: El objeto del cliente.
        productos: Una lista de los objetos de producto.

    Returns:
        El objeto Factura recién creado.
    """
    # 1. Obtener un nuevo número de control.
    log.info("Solicitando nuevo número de control...")
    numero_control = get_next_control_number()
    log.info(f"Número de control asignado: {numero_control}")

    # 2. Crear el objeto Factura.
    factura = Factura(
        emisor=emisor,
        cliente=cliente,
        productos=productos,
        numero_control=numero_control
    )
    log.info(f"Objeto de factura creado para el cliente '{cliente.nombre}' (ID: {cliente.identificacion}).")

    # 3. Generar el archivo XML.
    try:
        xml_filepath = generate_xml(factura)
        log.info(f"Factura {numero_control} generada exitosamente en XML: {xml_filepath}")
    except Exception as e:
        log.error(f"Fallo al generar el archivo XML para la factura {numero_control}: {e}")
        # Dependiendo del caso de uso, se podría querer anular el número de control
        # o manejar el error de otra forma. Por ahora, se relanza la excepción.
        raise

    # 4. Registrar la finalización del proceso.
    log.info(f"Proceso de facturación completado para la factura {numero_control}.")

    return factura
