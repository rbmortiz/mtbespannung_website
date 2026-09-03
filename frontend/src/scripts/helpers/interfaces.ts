export interface StringTypes {
    string_id: number;
    name: string;
    sport: string;
    color: string;
    description: string;
    type: string;
    price: number;
    is_active: Boolean;
    created_at: string;
    updated_at: string;
}

export interface StringingOrder {
    order_id: number;
    racket_name: string;
    additional_info: string | null;
    horizontal_kg: number | null;
    vertical_kg: number | null;
    string_id: number;
    status: string;
    created_at: string;
    updated_at: string;
}