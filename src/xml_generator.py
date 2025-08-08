import os
import xml.etree.ElementTree as ET
from xml.dom import minidom
from src.models import Factura

# Define la ruta al directorio de salida para los archivos XML.
DATA_DIR = os.path.join(os.path.dirname(__file__), '..', 'data', 'xml')

def generate_xml(factura: Factura) -> str:
    """
    Genera un archivo XML a partir de un objeto Factura y lo guarda.
    Utiliza minidom para formatear el XML y hacerlo legible.

    Args:
        factura: El objeto Factura que se va a serializar.

    Returns:
        La ruta al archivo XML generado.
    """
    if not os.path.exists(DATA_DIR):
        os.makedirs(DATA_DIR)

    # Crea el elemento raíz <Factura>.
    root = ET.Element("Factura")
    root.set("Version", "1.0")

    # --- Encabezado de la Factura ---
    header = ET.SubElement(root, "Encabezado")
    ET.SubElement(header, "TipoDocumento").text = "Factura"
    ET.SubElement(header, "NumeroControl").text = factura.numero_control
    ET.SubElement(header, "FechaEmision").text = factura.fecha_emision
    ET.SubElement(header, "HoraEmision").text = factura.hora_emision

    # --- Datos del Emisor ---
    emisor = ET.SubElement(root, "Emisor")
    ET.SubElement(emisor, "Nombre").text = factura.emisor.nombre
    ET.SubElement(emisor, "RIF").text = factura.emisor.rif
    ET.SubElement(emisor, "DomicilioFiscal").text = factura.emisor.domicilio_fiscal

    # --- Datos del Receptor (Cliente) ---
    receptor = ET.SubElement(root, "Receptor")
    ET.SubElement(receptor, "Nombre").text = factura.cliente.nombre
    ET.SubElement(receptor, "Identificacion").text = factura.cliente.identificacion
    ET.SubElement(receptor, "DomicilioFiscal").text = factura.cliente.domicilio_fiscal
    ET.SubElement(receptor, "Telefono").text = factura.cliente.telefono
    ET.SubElement(receptor, "Email").text = factura.cliente.email

    # --- Líneas de Productos/Items ---
    items = ET.SubElement(root, "Items")
    for prod in factura.productos:
        item = ET.SubElement(items, "Item")
        ET.SubElement(item, "Descripcion").text = prod.descripcion
        ET.SubElement(item, "Cantidad").text = str(prod.cantidad)
        ET.SubElement(item, "PrecioUnitario").text = f"{prod.precio_unitario:.2f}"
        ET.SubElement(item, "AlicuotaIVA").text = str(prod.alicuota_iva)
        ET.SubElement(item, "PrecioTotal").text = f"{prod.precio_total:.2f}"

    # --- Totales de la Factura ---
    totales = ET.SubElement(root, "Totales")
    ET.SubElement(totales, "BaseImponible").text = f"{factura.base_imponible:.2f}"
    ET.SubElement(totales, "TotalIVA").text = f"{factura.total_iva:.2f}"
    ET.SubElement(totales, "MontoTotal").text = f"{factura.monto_total:.2f}"

    # --- Datos de la Imprenta Digital ---
    imprenta = ET.SubElement(root, "ImprentaDigital")
    ET.SubElement(imprenta, "Nombre").text = factura.imprenta_nombre
    ET.SubElement(imprenta, "RIF").text = factura.imprenta_rif
    ET.SubElement(imprenta, "Autorizacion").text = factura.imprenta_autorizacion

    # Convierte el árbol XML a una cadena y la formatea.
    xml_str = ET.tostring(root, 'utf-8')
    parsed_str = minidom.parseString(xml_str)
    pretty_xml_str = parsed_str.toprettyxml(indent="  ")

    # Guarda el XML formateado en un archivo.
    filename = f"FACTURA_{factura.numero_control}.xml"
    filepath = os.path.join(DATA_DIR, filename)

    with open(filepath, "w", encoding="utf-8") as f:
        f.write(pretty_xml_str)

    return filepath
