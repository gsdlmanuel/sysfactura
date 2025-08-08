from src.models import EmpresaEmisora, Cliente, Producto
from src.invoice_manager import create_new_invoice
from src.email_sender import send_invoice_email
from src.logger import log

def main():
    """
    Punto de entrada principal para demostrar el sistema de facturación.
    Este script simula la creación de una factura y la guarda en XML.
    """
    log.info("==================================================")
    log.info("Iniciando nuevo proceso de facturación de demostración")

    # --- 1. Definir los datos para la factura ---
    # Datos de la empresa que emite la factura.
    emisor = EmpresaEmisora(
        nombre="Mi Empresa de Tecnología, C.A.",
        rif="J-29876543-2",
        domicilio_fiscal="Av. Libertador, Torre Principal, Piso 10, Oficina 10A, Caracas, Venezuela"
    )

    # Datos del cliente que recibe la factura.
    cliente = Cliente(
        nombre="Servicios Creativos Globales, S.R.L.",
        identificacion="J-40123456-7",
        domicilio_fiscal="Calle La Amargura, Edificio El Sol, Piso 3, Valencia, Venezuela",
        telefono="0412-9876543",
        email="pagos@servicioscreativos.com"  # Email del destinatario para el envío.
    )

    # Lista de productos o servicios incluidos en la factura.
    productos = [
        Producto(descripcion="Servicio de Consultoría Estratégica", cantidad=15, precio_unitario=80.0, alicuota_iva=16.0),
        Producto(descripcion="Licencia de Software 'Creativo PRO'", cantidad=2, precio_unitario=450.0, alicuota_iva=16.0),
        Producto(descripcion="Capacitación y Formación (Exento de IVA)", cantidad=1, precio_unitario=500.0, alicuota_iva=0.0)
    ]

    log.info("Datos de la factura definidos. Procediendo a crear la factura...")
    print("Preparando para crear una nueva factura...")

    # --- 2. Orquestar la creación de la factura ---
    try:
        factura_creada = create_new_invoice(emisor, cliente, productos)

        print("\n¡Factura creada con éxito!")
        print(f"  Número de Control: {factura_creada.numero_control}")
        print(f"  Fecha de Emisión: {factura_creada.fecha_emision}")
        print(f"  Cliente: {factura_creada.cliente.nombre}")
        print(f"  Monto Total: {factura_creada.monto_total:.2f} Bs.D.")

        xml_path = f"data/xml/FACTURA_{factura_creada.numero_control}.xml"
        print(f"  Archivo XML guardado en: {xml_path}")

        # --- 3. Enviar la factura por correo (Opcional) ---
        # Para habilitar esta funcionalidad, asegúrese de haber configurado
        # las variables de entorno 'GMAIL_SENDER_EMAIL' y 'GMAIL_APP_PASSWORD'.
        # print("\nIntentando enviar factura por correo...")
        # send_invoice_email(
        #     recipient_email=cliente.email,
        #     xml_filepath=xml_path
        # )

    except Exception as e:
        log.critical(f"Ocurrió un error crítico durante la creación de la factura: {e}", exc_info=True)
        print(f"\nError: No se pudo crear la factura. Revise 'logs/activity.log' para más detalles.")

    log.info("Proceso de facturación de demostración finalizado.")
    log.info("==================================================\n")

if __name__ == "__main__":
    main()
